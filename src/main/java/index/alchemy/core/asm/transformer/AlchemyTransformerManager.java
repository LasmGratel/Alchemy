package index.alchemy.core.asm.transformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Patch;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyCorePlugin;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.ReflectionHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.core.AlchemyConstants.*;

@Omega
public class AlchemyTransformerManager implements IClassTransformer {
	
	public static final String 
			ALCHEMY_HOOKS_CLASS = "index.alchemy.core.AlchemyHooks",
			ALCHEMY_HOOKS_DESC = "index/alchemy/core/AlchemyHooks",
			HOOK_PROVIDER_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Hook$Provider;",
			HOOK_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Hook;",
			HOOK_RESULT_DESC = "index/alchemy/api/annotation/Hook$Result",
			PATCH_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch;",
			PATCH_EXCEPTION_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch$Exception;",
			FIELD_PROVIDER_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Field$Provider;",
			FIELD_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Field;",
			FIELD_ACCESS_DESC = "Lindex/alchemy/api/IFieldAccess;",
			SIDE_ONLY_ANNOTATION_DESC = "Lnet/minecraftforge/fml/relauncher/SideOnly;";
	
	public static final Logger logger = LogManager.getLogger(AlchemyTransformerManager.class.getSimpleName());
	
	public static final Set<IClassTransformer> transformers = Sets.newHashSet();
	public static final Map<String, List<IClassTransformer>> transformers_mapping = new HashMap<String, List<IClassTransformer>>() {
		@Override
		public List<IClassTransformer> get(Object key) {
			List<IClassTransformer> result = super.get(key);
			if (result == null)
				put((String) key, result = new LinkedList());
			return result;
		}
	};
	static {
		try {
			ReflectionHelper.setClassLoader(ClassWriter.class, AlchemyCorePlugin.getLaunchClassLoader());
			loadAllProvider();
			loadAllTransform();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final boolean debug_print = Boolean.getBoolean("index.alchemy.core.asm.classloader.at.debug.print");
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadAllProvider() throws IOException {
		ClassPath path = ClassPath.from(AlchemyTransformerManager.class.getClassLoader());
		for (ClassInfo info : path.getAllClasses())
			if (info.getName().startsWith(MOD_PACKAGE)) {
				ClassReader reader = new ClassReader(info.url().openStream());
				ClassNode node = new ClassNode(ASM5);
				reader.accept(node, 0);
				if (checkSideOnly(node)) {
					loadPatch(node);
					loadField(node);
					loadHook(node);
				}
			}
	}
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadPatch(ClassNode node) throws IOException {
		if (node.visibleAnnotations != null)
			for (AnnotationNode ann : node.visibleAnnotations)
				if (ann.desc.equals(PATCH_ANNOTATION_DESC)) {
					Patch patch = Tool.makeAnnotation(Patch.class, ann.values);
					transformers_mapping.get(patch.value()).add(new TransformerPatch(node));
					break;
				}
	}
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadHook(ClassNode node) throws IOException {
		if (node.visibleAnnotations != null)
			for (AnnotationNode nann : node.visibleAnnotations)
				if (nann.desc.equals(HOOK_PROVIDER_ANNOTATION_DESC)) {
					for (MethodNode methodNode : node.methods)
						if (checkSideOnly(methodNode) && methodNode.visibleAnnotations != null)
							for (AnnotationNode ann : methodNode.visibleAnnotations)
								if (ann.desc.equals(HOOK_ANNOTATION_DESC)) {
									Hook hook = Tool.makeAnnotation(Hook.class, ann.values,
											"isStatic", false, "type", Hook.Type.HEAD, "disable", "");
									if (hook.disable().isEmpty() || !Boolean.getBoolean(hook.disable())) {
										String args[] = hook.value().split("#");
										if (args.length == 2)
											transformers_mapping.get(args[0]).add(new TransformerHook(methodNode, node.name,
													args[0], args[1], hook.isStatic(), hook.type()));
										else
											AlchemyRuntimeException.onException(new RuntimeException("@Hook method -> split(\"#\") != 2"));
									}
								}
					break;
				}
	}
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadField(ClassNode node) throws IOException {
		if (node.visibleAnnotations != null)
			for (AnnotationNode nann : node.visibleAnnotations)
				if (nann.desc.equals(FIELD_PROVIDER_ANNOTATION_DESC)) {
					for (FieldNode fieldNode : node.fields)
						if (checkSideOnly(fieldNode) && fieldNode.desc.equals(FIELD_ACCESS_DESC)) {
							String generics[] = ASMHelper.getGenericType(ASMHelper.getGeneric(fieldNode.signature));
							if (generics.length > 1)
								transformers_mapping.get(ASMHelper.getClassName(generics[0]).replace("/", "."))
										.add(new TransformerFieldAccess(node.name.replace('/', '.'), fieldNode));
						}
					break;
				}
	}
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadAllTransform() throws IOException {
		ClassPath path = ClassPath.from(AlchemyTransformerManager.class.getClassLoader());
		for (ClassInfo info : path.getTopLevelClassesRecursive(MOD_TRANSFORMER_PACKAGE.substring(0, MOD_TRANSFORMER_PACKAGE.length() - 1))) {
			ClassReader reader = new ClassReader(info.url().openStream());
			ClassNode node = new ClassNode(ASM5);
			reader.accept(node, 0);
			if (checkSideOnly(node)) {
				Class clazz = info.load();
				if (Tool.isInstance(IAlchemyClassTransformer.class, clazz))
					try {
						IAlchemyClassTransformer transformer = (IAlchemyClassTransformer) clazz.newInstance();
						if (!transformer.disable())
							if (transformer.getTransformerClassName() == null)
								transformers.add(transformer);
							else
								transformers_mapping.get(transformer.getTransformerClassName()).add(transformer);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
			}
		}
	}
	
	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (debug_print)
			logger.info("loading: " + transformedName);
		byte[] old = basicClass;
		for (IClassTransformer transformer : transformers)
			basicClass = transformer.transform(name, transformedName, basicClass);
		if (transformers_mapping.containsKey(transformedName))
			for (IClassTransformer transformer : transformers_mapping.get(transformedName))
				basicClass = transformer.transform(name, transformedName, basicClass);
		if (transformedName.equals("index.alchemy.item.ItemRingAlive"))
			Tool.dumpClass(basicClass, "D:/ItemRingAlive.bytecode");
		return basicClass;
	}
	
	public static boolean checkSideOnly(ClassNode node) {
		if (node.visibleAnnotations == null)
			return true;
		for (AnnotationNode annotation : node.visibleAnnotations)
			if (annotation.desc.equals(SIDE_ONLY_ANNOTATION_DESC))
				return Tool.makeAnnotation(SideOnly.class, annotation.values).value() == AlchemyCorePlugin.runtimeSide();
		return true;
	}
	
	public static boolean checkSideOnly(MethodNode node) {
		if (node.visibleAnnotations == null)
			return true;
		for (AnnotationNode annotation : node.visibleAnnotations)
			if (annotation.desc.equals(SIDE_ONLY_ANNOTATION_DESC))
				return Tool.makeAnnotation(SideOnly.class, annotation.values).value() == AlchemyCorePlugin.runtimeSide();
		return true;
	}
	
	public static boolean checkSideOnly(FieldNode node) {
		if (node.visibleAnnotations == null)
			return true;
		for (AnnotationNode annotation : node.visibleAnnotations)
			if (annotation.desc.equals(SIDE_ONLY_ANNOTATION_DESC))
				return Tool.makeAnnotation(SideOnly.class, annotation.values).value() == AlchemyCorePlugin.runtimeSide();
		return true;
	}
	
	public static void loadAllTransformClass() {
		for (String clazz : transformers_mapping.keySet())
			try {
				Class.forName(clazz, false, AlchemyTransformerManager.class.getClassLoader());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	}
	
	public static void transform(String name) {
		logger.info("Transform: " + name);
	}

}
