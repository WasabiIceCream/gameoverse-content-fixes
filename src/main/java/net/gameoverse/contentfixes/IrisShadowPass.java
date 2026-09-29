package net.gameoverse.contentfixes;

import net.fabricmc.loader.api.FabricLoader;

/** Whether Iris is rendering its shadow pass. Iris classes are only touched when Iris is loaded. */
public final class IrisShadowPass {
    private static final boolean IRIS = FabricLoader.getInstance().isModLoaded("iris");

    private IrisShadowPass() {}

    public static boolean active() {
        return IRIS && Api.active();
    }

    private static final class Api {
        static boolean active() {
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isRenderingShadowPass();
        }
    }
}
