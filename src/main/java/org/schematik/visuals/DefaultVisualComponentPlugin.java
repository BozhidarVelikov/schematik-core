package org.schematik.visuals;

import com.google.gson.Gson;
import org.schematik.gson.GsonUtils;
import org.schematik.jetty.JettyServer;
import org.schematik.plugin.ISchematikPlugin;
import org.schematik.visuals.component.AbstractVisualComponent;

import java.util.Objects;

public class DefaultVisualComponentPlugin implements ISchematikPlugin {
    Gson gson;

    @Override
    public void register() {
        gson = GsonUtils.getDefaultGson();

        JettyServer.instance.app.get(
                "api/{component}/{action}",
                context -> {
                    Class<?> componentClass = Class.forName(context.pathParam("component"));

                    if (AbstractVisualComponent.class.isAssignableFrom(componentClass)) {
                        AbstractVisualComponent componentInstance = (AbstractVisualComponent) componentClass
                                .getConstructor(String.class)
                                .newInstance(context.body());
                        componentClass.getMethod(context.pathParam("action")).invoke(componentInstance);

                        context.result(Objects.requireNonNull(componentClass
                                .getMethod("getState")
                                .invoke(componentInstance)
                        ).toString());
                    }
                }
        );
    }


}
