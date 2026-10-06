package net.gameoverse.contentfixes;

import static org.lwjgl.system.MemoryUtil.memAddress;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.lwjgl.system.JNI;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.SharedLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Japanese, Chinese and Korean input on Linux without launcher settings. Minecraft 26.1's GLFW reaches an input
 * method through XIM when it runs on X11 (XWayland included), and XIM only finds the input method when
 * {@code XMODIFIERS} names it. Desktops that set up Fcitx or IBus usually export it, but not all do (niri doesn't),
 * and then the input method can't be switched on in game at all. If {@code XMODIFIERS} is unset and Fcitx or IBus is
 * running, this sets it in the process environment before GLFW starts (a preLaunch entrypoint, through libc's
 * {@code setenv}). Anything already set is left alone; other systems are untouched.
 */
public final class ImeEnvironment implements PreLaunchEntrypoint {
    private static final Logger LOG = LoggerFactory.getLogger("gameoverse_content_fixes");

    @Override
    public void onPreLaunch() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) return;
        String current = System.getenv("XMODIFIERS");
        if (current != null && !current.isBlank()) return;
        String im = runningInputMethod();
        if (im == null) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SharedLibrary libc = Library.loadNative(ImeEnvironment.class, "org.lwjgl", "libc.so.6");
            long setenv = libc.getFunctionAddress("setenv");
            if (setenv == 0L) return;
            // int setenv(const char *name, const char *value, int overwrite)
            int result = JNI.invokePPI(memAddress(stack.UTF8("XMODIFIERS")), memAddress(stack.UTF8("@im=" + im)), 1, setenv);
            LOG.info("Input method: {} is running and XMODIFIERS was unset, set it to @im={} ({})", im, im, result == 0 ? "ok" : "failed");
        } catch (Throwable t) {
            LOG.warn("Input method: couldn't set XMODIFIERS: {}", t.toString());
        }
    }

    /** "fcitx" if Fcitx (4 or 5) is running, else "ibus" if IBus is, else null. */
    private static String runningInputMethod() {
        boolean ibus = false;
        try (DirectoryStream<Path> procs = Files.newDirectoryStream(Path.of("/proc"), p -> p.getFileName().toString().chars().allMatch(Character::isDigit))) {
            for (Path proc : procs) {
                String name;
                try {
                    name = Files.readString(proc.resolve("comm")).trim();
                } catch (IOException e) {
                    continue;
                }
                if (name.equals("fcitx5") || name.equals("fcitx")) return "fcitx";
                if (name.equals("ibus-daemon")) ibus = true;
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
        return ibus ? "ibus" : null;
    }
}
