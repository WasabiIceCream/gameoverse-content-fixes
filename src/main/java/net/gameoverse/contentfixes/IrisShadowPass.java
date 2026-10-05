package net.gameoverse.contentfixes;

import net.fabricmc.loader.api.FabricLoader;

/** Whether Iris is rendering its shadow pass, or has a shader pack on. Iris classes are only touched when Iris is loaded. */
public final class IrisShadowPass {
    private static final boolean IRIS = FabricLoader.getInstance().isModLoaded("iris");

    private IrisShadowPass() {}

    public static boolean active() {
        return IRIS && Api.active();
    }

    public static boolean shaderPackInUse() {
        return IRIS && Api.packInUse();
    }

    private static final class Api {
        static boolean active() {
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isRenderingShadowPass();
        }

        static boolean packInUse() {
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
        }
    }
}
