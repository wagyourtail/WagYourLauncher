package xyz.wagyourtail.launcher.minecraft.auth.common;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.minecraft.auth.AbstractStep;

@SuppressWarnings("rawtypes")
public record MCToken(AbstractStep step, String access_token, String token_type, long expireTime, String user_type, AbstractStep.StepResult<?, ?> prev) implements AbstractStep.StepResult<Object, AbstractStep.StepResult<?, ?>> {
    @Override
    public AbstractStep<AbstractStep.StepResult<?, ?>, AbstractStep.StepResult<Object, AbstractStep.StepResult<?, ?>>> getStep() {
        return step;
    }

    @Override
    public AbstractStep.StepResult<?, ?> getPrevResult() {
        return prev;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("access_token", access_token);
        json.addProperty("token_type", token_type);
        json.addProperty("expireTime", expireTime);
        json.addProperty("user_type", user_type);
        json.add("prev", prev.toJson());
        return json;
    }

}
