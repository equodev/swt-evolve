package org.eclipse.swt.program;

class Impls {

    static IProgram newProgram(org.eclipse.swt.program.Program api) {
        return new DartProgram(api);
    }

    static class Program {

        static org.eclipse.swt.program.Program findProgram(String extension) {
            return DartProgram.findProgram(extension);
        }

        static String[] getExtensions() {
            return DartProgram.getExtensions();
        }

        static org.eclipse.swt.program.Program[] getPrograms() {
            return DartProgram.getPrograms();
        }

        static boolean launch(String fileName) {
            return DartProgram.launch(fileName);
        }

        static boolean launch(String fileName, String workingDir) {
            return DartProgram.launch(fileName, workingDir);
        }
    }
}
