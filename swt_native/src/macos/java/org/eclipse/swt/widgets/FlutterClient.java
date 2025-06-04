package org.eclipse.swt.widgets;

import static java.util.Arrays.asList;

import com.equo.comm.api.ICommService;
import com.equo.comm.ws.provider.EquoWebSocketServiceImpl;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FlutterClient implements Delegate {

    public enum Mode {
        Native, Browser, Equo;

        public static Mode of(String anyCase) {
            return valueOf(anyCase.substring(0, 1).toUpperCase() + anyCase.substring(1).toLowerCase());
        }

    }

    static Delegate defaultDelegate;
    private Delegate delegate;

    private Process proc;
    private final Mode clientMode;
    private final String runMode;
    private String testName;
    private String testFile;
    private List<String> tests;
    private static int DART_PORT = 40899;
    ICommService comm;
    private CompletableFuture<Boolean> ready;
    private boolean sizing;
    private String theme = System.getProperty("equo.theme", "light");

    private long shellId;

    public void setShell(long id) {
        this.shellId = id;
    }

    public FlutterClient() {
        this(Mode.of(System.getProperty("equo.swt.client", Mode.Native.name())));
        delegate = defaultDelegate;
    }

    public FlutterClient(Mode mode) {
        clientMode = mode;
        runMode = System.getProperty("equo.swt.debug", "dev");
        delegate = null;
    }

    public FlutterClient forSizes() {
        sizing = true;
        return this;
    }

    public ICommService getComm() {
        return comm;
    }

    public ICommService createComm() {
        if (delegate != null)
            return comm = delegate.createComm();
        return comm = doCreateComm();
    }

    private ICommService doCreateComm() {
        String impl = (clientMode == Mode.Equo) ? "com.equo.comm.ee.provider.service.ChromiumService"
                : "com.equo.comm.ws.provider.EquoWebSocketServiceImpl";
        String sendImpl = (clientMode == Mode.Equo) ? "com.equo.comm.ee.provider.service.ChromiumSendService"
                : "com.equo.comm.ws.provider.EquoWebSocketServiceImpl";
        System.setProperty("com.equo.comm.api.ICommService", impl);
        System.setProperty("com.equo.comm.api.ICommSendService", sendImpl);
        ServiceLoader<ICommService> serviceLoader = ServiceLoader.load(ICommService.class);
        return serviceLoader.stream().filter(p -> impl.equals(p.type().getName())).map(ServiceLoader.Provider::get)
                .findFirst().orElse(null);
    }

    public void createClient() throws Exception {
        if (delegate != null) {
            delegate.createClient();
            return;
        }
        doCreateClient();
    }

    private void doCreateClient() throws Exception {
        DART_PORT++;
        switch (clientMode) {
            case Browser:
//			openBrowser();
                break;
            case Native:
                openNative();
                break;
        }
    }

    private boolean isTest() {
        return false;
    }

    private List<String> flutterNativeArgs(int port, String... extra) {
        List<String> args = asList("flutter", "--no-version-check", isTest() ? "test" : "run", "-d", "linux"
//				Utils.getOS()
        );
        if (isTest())
            args.addAll(flutterTestArgs(port));
        else {
            args.addAll(asList("--dart-entrypoint-args", String.valueOf(port)));
            if (sizing)
                args.addAll(asList("--dart-entrypoint-args", "true"));
        }
        args.addAll(asList("--dart-entrypoint-args", Long.toString(shellId)));
        args.addAll(asList("--dart-entrypoint-args", theme));
        args.addAll(asList(extra));
        return args;
    }

    private List<String> flutterTestCommonArgs(int commPort) {
        return List.of("--dart-define=equo.comm_port=" + commPort, "--dart-define=equo.mode=" + clientMode,
                "--dart-define=equo.tests=" + String.join(",", tests), testFile + ".dart");
    }

    private List<String> flutterTestArgs(int commPort) {
        List<String> args = new ArrayList<>(asList("-r", "expanded", "--no-test-assets"));
        args.addAll(flutterTestCommonArgs(commPort));
        return args;
    }

    private List<String> flutterWebArgs(int port, String device, String... extra) {
        List<String> args = new ArrayList<>(
                asList("flutter", "--no-version-check", isTest() && Mode.Equo != clientMode ? "test" : "run"));
        if (isTest() && Mode.Equo != clientMode) {
            args.addAll(asList("--platform", device));
            args.addAll(flutterTestArgs(port));
        } else {
            args.addAll(asList("-d", device, "--web-port", String.valueOf(DART_PORT)));
            if (isTest())
                args.addAll(flutterTestCommonArgs(0));
            else if ("chrome".equals(device)) {
                args.addAll(asList("--web-launch-url",
                        getUrl() + "?equocommport=" + port + "&shellId=" + shellId + "&theme=" + theme));
            }
        }
        args.addAll(asList(extra));
        return args;
    }

    private void openNative() throws Exception {
        int port = getPort();
        File cwd = null;
        List<String> cmds = List.of();
        switch (runMode) {
            case "run":
//			Path p = Paths.get(Utils.getOS(), Utils.getArch());
//			Path osPath;
//			if (Utils.isMac())
//				osPath = Paths.get("swtflutter.app", "Contents", "MacOS", "swtflutter");
//			else if (Utils.isWindows())
//				osPath = Paths.get("swtflutter.exe");
//			else
//				osPath = Paths.get("swtflutter");
//			p = p.resolve(osPath);
//			if (!Files.exists(p)) { // dev
//				p = flutterPath().resolve(Paths.get("build", Utils.getOS()));
//				if (Utils.isMac())
//					p = p.resolve(Paths.get("Build", "Products", "Release").resolve(osPath));
//				else
//					p = p.resolve(Paths.get(Utils.getArch(), "release", "bundle").resolve(osPath));
//			}
//			cmds = List.of(p.toString(), String.valueOf(port), Long.toString(shellId), theme);
                break;
            case "dev":
                cwd = flutterPath().toFile();
                cmds = flutterNativeArgs(port);
                break;
            case "debug":
                cwd = flutterPath().toFile();
                cmds = flutterNativeArgs(port, "--start-paused");
                break;
        }
        startProcess(cwd, cmds, true);
    }

    private static Path flutterPath() {
        Path p = Paths.get(System.getProperty("user.dir"), "swtflutter");
        if (Files.exists(p))
            return p;
        return Paths.get(System.getProperty("user.dir"), "..", "swtflutter");
    }

    private CompletableFuture<Boolean> startProcess(File cwd, List<String> cmds, boolean inherit) throws Exception {
        if (cmds.isEmpty())
            return null;
        ProcessBuilder builder = new ProcessBuilder().directory(cwd).command(cmds);
        if (!isTest() && inherit)
            builder.inheritIO();
        System.out.println(String.join(" ", cmds));
        proc = builder.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!isTest()) {
                System.out.println("Shutdown");
                proc.destroy();
            }
        }));
        if (isTest()) {
            inheritIO(proc.getErrorStream(), System.err);
            return inheritIO(proc.getInputStream(), System.out);
        }
        return null;
    }

    public int getPort() {
//        return 0;
        return ((com.equo.comm.ws.provider.EquoWebSocketServiceImpl) comm).getPort();
    }

//	private void openBrowser() throws Exception {
//		int port = getPort();
//		File cwd = null;
//		List<String> cmds = List.of();
//		switch (runMode) {
//		case "run":
//			Path basedir = getWebDir();
//			http = new SimpleHttpServer(basedir);
//			int httpPort = http.start();
//			String browser = System.getProperty("equo.swt.browser",
//					(Utils.isMac()) ? "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" : "google-chrome");
//			String url = "http://localhost:" + httpPort + "?equocommport=" + port + "&shellId=" + shellId + "&theme="
//					+ theme;
//			if (!"none".equalsIgnoreCase(browser))
//				cmds = List.of(browser, url);
//			else
//				System.out.println("App ready, open: " + url);
//			break;
//		case "dev":
//			cwd = flutterPath().toFile();
//			cmds = flutterWebArgs(port, "chrome");
//			break;
//		case "debug":
//			cwd = flutterPath().toFile();
//			cmds = flutterWebArgs(port, "chrome", "--start-paused");
//			break;
//		}
//		startProcess(cwd, cmds, true);
//	}

    private static Path getWebDir() {
        Path basedir = Paths.get("web");
        if (!Files.exists(basedir))
            basedir = flutterPath().resolve(Paths.get("build", "web"));
        return basedir;
    }

    private CompletableFuture<Boolean> inheritIO(final InputStream src, final PrintStream dest) {
        CompletableFuture<Boolean> launched = new CompletableFuture<>();
        new Thread(() -> {
            Scanner sc = new Scanner(src);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains(getUrl()))
                    launched.complete(true);
                else if (isTest())
                    processLine(line);
                dest.println(line);
            }
        }).start();
        return launched;
    }

    private void processLine(String line) {
        if (line.contains("== Ready " + clientMode)) {
            ready.complete(true);
        }
    }

    private String getUrl() {
        return "http://localhost:" + DART_PORT;
    }

    public boolean isAlive() {
        if (delegate != null)
            return delegate.isAlive();
        return doIsAlive();
    }

    private boolean doIsAlive() {
        return !"native".equals(runMode) || (proc == null || proc.isAlive());
    }

    public void dispose() {
        if (delegate != null) {
            delegate.dispose();
            return;
        }
        doDispose();
    }

    private void doDispose() {
        try {
            if (isTest()) {
                if (Mode.Equo == clientMode) {
                }
                try {
                    proc.waitFor(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (proc != null) {
                proc.destroy();
                try {
                    proc.waitFor(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (proc.isAlive())
                    throw new RuntimeException("Failed to terminate proccess: " + proc.info());
            }
        } finally {
            if ((Mode.Native == clientMode || Mode.Browser == clientMode) && !isTest()) {
                // kill the WS thread
                ((EquoWebSocketServiceImpl) comm).stop();
//            System.exit(0);
            }
//            if (Mode.Browser ==  clientMode && http != null) {
//                http.stop();
//            }
        }
    }

    public FlutterClient forTests(List<String> tests) {
        ready = new CompletableFuture<>();
//		this.tests = tests;
        return this;
    }

    public FlutterClient forTest(Method info) {
        testName = info.getName();
        Class<?> declaringClass = getTopLevelClass(info.getDeclaringClass());
        String folder = declaringClass.getPackageName().replace(".", "/");
        String file = dartFile(declaringClass.getSimpleName());
        testFile = "test/" + folder + "/" + file;
        return this;
    }

    public static Class<?> getTopLevelClass(Class<?> declaringClass) {
        while (declaringClass.isMemberClass()) {
            declaringClass = declaringClass.getDeclaringClass();
        }
        return declaringClass;
    }

    public static String dartFile(String classSimpleName) {
        return classSimpleName.replace("Test", "_test").toLowerCase();
    }

    public void startTest(boolean signalStart) {
        if (!isTest())
            throw new IllegalStateException("missing test name");
        try {
            ready.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (signalStart)
            signalStart();
    }

    public void signalStart() {
        try {
            System.out.println("Signal start " + testEventName());
            comm.send(testEventName());
        } catch (Exception e) {
            throw new AssertionError("Dart Test did not start");
        }
    }

    private String testEventName() {
        return Path.of(testFile).getFileName() + "/" + testName + "#" + clientMode;
    }

    public void endTest() {
        try {
            String result = "not started";
            if (!"success".equals(result.trim())) {
                // proc.waitFor(30, TimeUnit.SECONDS);
                throw new AssertionError("flutter test result was " + result);
            }
            // if (!proc.waitFor(60, TimeUnit.SECONDS))
            // throw new AssertionError("flutter didn't finished");
            // if (proc.exitValue() != 0)
            // throw new AssertionError("flutter failed with exit code "+proc.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

}
