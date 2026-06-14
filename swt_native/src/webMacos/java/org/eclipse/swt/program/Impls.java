package org.eclipse.swt.program;

import dev.equo.swt.Config;

class Impls {

    static IProgram newProgram(org.eclipse.swt.program.Program api) {
        return Config.isEquo(org.eclipse.swt.program.Program.class) ? new DartProgram(api) : new SwtProgram(api);
    }

    static class Program {

        static org.eclipse.swt.program.Program findProgram(String extension) {
            return Config.isEquo(org.eclipse.swt.program.Program.class) ? DartProgram.findProgram(extension) : SwtProgram.findProgram(extension);
        }

        static String[] getExtensions() {
            return Config.isEquo(org.eclipse.swt.program.Program.class) ? DartProgram.getExtensions() : SwtProgram.getExtensions();
        }

        static org.eclipse.swt.program.Program[] getPrograms() {
            return Config.isEquo(org.eclipse.swt.program.Program.class) ? DartProgram.getPrograms() : SwtProgram.getPrograms();
        }

        static boolean launch(String fileName) {
            return Config.isEquo(org.eclipse.swt.program.Program.class) ? DartProgram.launch(fileName) : SwtProgram.launch(fileName);
        }

        static boolean launch(String fileName, String workingDir) {
            return Config.isEquo(org.eclipse.swt.program.Program.class) ? DartProgram.launch(fileName, workingDir) : SwtProgram.launch(fileName, workingDir);
        }
    }
}
