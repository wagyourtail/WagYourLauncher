package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.gui.windows.login.GuiLogin;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;

public abstract class AbstractStep<T extends AbstractStep.StepResult<?, ?>, U extends AbstractStep.StepResult<?, ?>> {
    public final Launcher launcher;
    public final AbstractStep<?, T> prevStep;
    public AbstractStep(Launcher launcher, AbstractStep<?, T> prevStep) {
        this.launcher = launcher;
        this.prevStep = prevStep;
    }

    public abstract U applyStep(T prev_result, Logger logger) throws IOException;

    public U refresh(U result, Logger logger) throws IOException {
        return applyStep(prevStep.refresh((T) result.getPrevResult(), logger), logger);
    }

    public abstract U fromJson(JsonObject json) throws MalformedURLException;

    public interface StepResult<T, U extends StepResult<?, ?>> {
        AbstractStep<U, StepResult<T, U>> getStep();

        U getPrevResult();
        default T getResult() {
            return (T) this;
        }

        JsonObject toJson();
    }
}
