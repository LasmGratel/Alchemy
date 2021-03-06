package index.alchemy.core.asm.transformer;

import java.util.Iterator;
import java.util.LinkedList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.TypeInsnNode;

import index.alchemy.api.annotation.SideOnlyLambda;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.core.AlchemyConstants.*;

@Omega
public class TransformerSideLambda implements IClassTransformer {
	
	public static final String SIDE_ONLY_LAMBDA_ANNOTATION_DESC = ASMHelper.getClassDesc("index.alchemy.api.annotation.SideOnlyLambda");
	
	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.startsWith(MOD_PACKAGE))
			return basicClass;
		LinkedList<Type> types = new LinkedList<>();
		LinkedList<Boolean> marks = new LinkedList<>();
		int flag = -1;
		ClassNode node = ASMHelper.getClassNode(transformedName);
		for (Iterator<MethodNode> iterator = node.methods.iterator(); iterator.hasNext();) {
			MethodNode method = iterator.next();
			Side side = null;
			if (method.visibleAnnotations != null)
				for (AnnotationNode annotation : method.visibleAnnotations)
					if (annotation.desc.equals(AlchemyTransformerManager.SIDE_ONLY_ANNOTATION_DESC))
						side = Tool.makeAnnotation(SideOnly.class, annotation.values).value();
			for (Iterator<AbstractInsnNode> insnIterator = method.instructions.iterator(); insnIterator.hasNext(); flag--) {
				AbstractInsnNode insn = insnIterator.next();
				if (insn instanceof InvokeDynamicInsnNode) {
					InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) insn;
					boolean lambda = false;
					for (int i = 0; i < dynamic.bsmArgs.length; i++)
						if (dynamic.bsmArgs[i] instanceof Handle) {
							Handle handle = (Handle) dynamic.bsmArgs[i];
							if (handle.getOwner().equals(node.name) && handle.getName().startsWith("lambda$"))
								lambda = true;
						}
					if (!lambda)
						continue;
					Type type = Type.getReturnType(dynamic.desc);
					types.add(type);
					marks.add(side != null && side != AlchemyEngine.runtimeSide());
					flag = 3;
				}
				if (flag > -1 && !marks.getLast() && insn instanceof TypeInsnNode) {
					flag = -1;
					TypeInsnNode type = (TypeInsnNode) insn;
					if (Type.getType(ASMHelper.getClassDesc(type.desc)).equals(types.getLast()) &&
							insn.visibleTypeAnnotations != null)
						for (TypeAnnotationNode ann : insn.visibleTypeAnnotations)
							if (ann.desc.equals(SIDE_ONLY_LAMBDA_ANNOTATION_DESC) &&
									Tool.makeAnnotation(SideOnlyLambda.class, ann.values).value() != AlchemyEngine.runtimeSide())
								marks.set(marks.size() - 1, true);
				}
			}
		}
		if (marks.isEmpty())
			return basicClass;
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = ASMHelper.newClassWriter(0);
		node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (Iterator<MethodNode> iterator = node.methods.iterator(); !marks.isEmpty() && iterator.hasNext();) {
			MethodNode method = iterator.next();
			if (method.name.startsWith("lambda$") && (method.access & ACC_SYNTHETIC) != 0) {
				if (marks.getFirst())
					iterator.remove();
				types.removeFirst();
				marks.removeFirst();
			}
		}
		node.accept(writer);
		return writer.toByteArray();
	}

}
