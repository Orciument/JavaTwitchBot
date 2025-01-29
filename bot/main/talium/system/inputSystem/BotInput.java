package talium.system.inputSystem;

public interface BotInput extends Runnable {
    @Override
    void run();

    void shutdown();
}
