package com.zyh.test.asm5;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AsmExample extends ClassLoader implements Opcodes {

	public static class Foo {
		public static void execute() {
			System.out.println("test changed method name");
		}

		public static boolean changeMethodContent() {
			System.out.println("test change method");
			return true;
		}

		private static boolean changeReplace() {
			System.out.println("test change method replace");
			return true;
		}
	}

	public static void main(String[] args) throws IOException, IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException {

		// ClassReader cr0 = new ClassReader(FooReplacement.class.getName());
		// ClassWriter cw0 = new ClassWriter(cr0, ClassWriter.COMPUTE_MAXS);
		// MethodChangeClassAdapter cv0 = new MethodChangeClassAdapter(cw0,
		// null);
		// cr0.accept(cv0, Opcodes.ASM5);

		ClassReader cr = new ClassReader(Foo.class.getName());
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
		ClassVisitor cv = new MethodChangeClassAdapter(cw, null);
		cr.accept(cv, Opcodes.ASM5);

		// 新增加一个方法
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "add", "([Ljava/lang/String;)V", null, null);
		// pushes the 'out' field (of type PrintStream) of the System class
		mw.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		// pushes the "Hello World!" String constant
		mw.visitLdcInsn("this is add method print!");
		// invokes the 'println' method (defined in the PrintStream class)
		mw.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		mw.visitInsn(RETURN);
		// this code uses a maximum of two stack elements and two local
		// variables
		mw.visitMaxs(0, 0);
		mw.visitEnd();

		// gets the bytecode of the Example class, and loads it dynamically
		byte[] code = cw.toByteArray();

		AsmExample loader = new AsmExample();
		Class<?> exampleClass = loader.defineClass(Foo.class.getName(), code, 0, code.length);

		for (Method method : exampleClass.getMethods()) {
			System.out.println(method);
		}

		System.out.println("*************");

		// uses the dynamically generated class to print 'Helloworld'
		exampleClass.getMethods()[0].invoke(null, null); // changeMethodContent

		System.out.println("*************");

		exampleClass.getMethods()[1].invoke(null, null);

		System.out.println("*************");

		exampleClass.getMethods()[2].invoke(null, new String[1]);

		// gets the bytecode of the Example class, and loads it dynamically

		FileOutputStream fos = new FileOutputStream("/tmp/Example.class");
		fos.write(code);
		fos.close();
	}

	static class MethodChangeClassAdapter extends ClassVisitor implements Opcodes {

		private final MethodChangeClassAdapter another;

		public MethodChangeClassAdapter(final ClassVisitor cv, final MethodChangeClassAdapter another) {
			super(Opcodes.ASM5, cv);
			this.another = another;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			if (cv != null) {
				cv.visit(version, access, name, signature, superName, interfaces);
			}
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (cv != null && "execute".equals(name)) { // when execute then
														// modify to execute1
				return cv.visitMethod(access, name + "1", desc, signature, exceptions);
			}

			if ("changeReplace".equals(name)) {
				if (another != null) {
					return another.visitMethod(access, "executeReplacement", desc, signature, exceptions);
				} else {
					return cv.visitMethod(access, "changeMethodContent", desc, signature, exceptions);
					// MethodVisitor mv = cv.visitMethod(access,
					// "changeReplace", desc, signature, exceptions);// 先得到原始的方法
					// MethodVisitor newMethod = null;
					// newMethod = new AsmMethodVisit(mv); // 访问需要修改的方法
					// return newMethod;
				}
			}
			if (cv != null && !name.equals("changeMethodContent")) {
				return cv.visitMethod(access, name, desc, signature, exceptions);
			}

			return null;
		}

	}

	static class AsmMethodVisit extends MethodVisitor {

		public AsmMethodVisit(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitCode() {
			super.visitCode();
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == Opcodes.RETURN) {
				// pushes the 'out' field (of type PrintStream) of the System
				// class
				mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				// pushes the "Hello World!" String constant
				mv.visitLdcInsn("this is a modify method!");
				// invokes the 'println' method (defined in the PrintStream
				// class)
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
				// mv.visitInsn(RETURN);
			}
			super.visitInsn(opcode);
		}
	}

}