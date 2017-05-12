
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.Class;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public class DA {

	private HashSet<Class> loadedPackage = new HashSet<>();

	public void loadPackage(String path) throws IOException, ClassNotFoundException {
		System.out.println("Looking for class files in " + path);
		FilenameFilter classFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".class");
			}
		};
		File f = new File(path);

		for (File file : f.listFiles(classFilter)) {
			String s = file.getName();
			s = s.replace(".class", "");
			s = f.getName() + "." + s;
			loadedPackage.add(Class.forName(s));
			System.out.println("  " + s);
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if (args.length != 1) {
			System.out.println("Usage FileLoader <path>");
			return;
		}

		DA main = new DA();
		main.loadPackage(args[0]);
		main.displayMetrics();
	}

	private int inDepth(Class c) {
		int d = 0;
		while (!c.getName().equals("java.lang.Object")) {
			d++;
			c = c.getSuperclass();
		}
		return d;
	}

	
	private double responsibility(Class c) {
		int clientsNumber = 0;
		for (Class d : loadedPackage) {
			boolean isClient = false;
			
			if(d.getSuperclass().equals(c)){
				isClient = true;
			}
			
			
			for (Method m : d.getDeclaredMethods()) {
				Class[] paramTypes = m.getParameterTypes();
				for (Class e : paramTypes) {
					if (loadedPackage.contains(e) && e.equals(c)) {
						isClient = true;
						break;
					}
				}
				Class r = m.getReturnType();
				if (loadedPackage.contains(c) && r.equals(c)) {
					isClient = true;
					break;

				}

			}

			for (Field f : d.getDeclaredFields()) {
				if (loadedPackage.contains(c) && f.getType().equals(c)) {
					isClient = true;
					break;
				}
			}

			if (isClient) {
				clientsNumber++;
			}
		}

		return ((double) clientsNumber) / loadedPackage.size();
	}

	private double instability(Class c) {
		int providersNumber = 0;
		if (inDepth(c) > 1) {
			providersNumber++;
		}
		HashSet<Class> set = new HashSet<>();
		for (Field f : c.getDeclaredFields()) {
			Class type = f.getType();

			if (loadedPackage.contains(type) && !set.contains(type)) {
				providersNumber++;
				set.add(type);
			}
		}
		for (Method m : c.getDeclaredMethods()) {
			Class[] paramTypes = m.getParameterTypes();
			for (Class t : paramTypes) {

				if (loadedPackage.contains(t) && !set.contains(t)) {
					providersNumber++;
					set.add(t);
				}
			}

			Class returnType = m.getReturnType();
			if (loadedPackage.contains(returnType) && !set.contains(returnType)) {
				providersNumber++;
				set.add(returnType);
			}
		}
		return ((double) providersNumber) / loadedPackage.size();
	}

	private double workload(Class c) {
		int packageMethodsNumber = 0;
		for (Class d : loadedPackage) {
			packageMethodsNumber += d.getDeclaredMethods().length;
		}
		return c.getDeclaredMethods().length / ((double) packageMethodsNumber);
	}

	public void displayMetrics() {

		System.out.println("C     inDepth(C)    instability(C)     responsibility(C)    workload(C)");

		for (Class c : loadedPackage) {
			System.out.format(
					"%s     %d         %.2f          %.2f          %.2f\n",
					c.getName(), 
					inDepth(c),
					instability(c),
					responsibility(c),
					workload(c));
					
					
					
		}
	}

}
