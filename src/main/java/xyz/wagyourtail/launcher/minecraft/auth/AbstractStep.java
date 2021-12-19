package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Launcher;

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

    public abstract U applyStep(T prev_result) throws IOException;

    public abstract U applyStep(T prev_result, Frame gui);

    public U refresh(U result) throws IOException {
        return applyStep(prevStep.refresh((T) result.getPrevResult()));
    }

    public U refreshGui(U result, Frame gui) {
        return applyStep(prevStep.refreshGui((T) result.getPrevResult(), gui), gui);
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
