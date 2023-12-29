package cn.revaria.seeder.command.argument;

public class DefaultOrIntegerArgument {
    public final boolean isDefault;
    public final int value;

    public DefaultOrIntegerArgument(boolean isDefault, int value) {
        this.isDefault = isDefault;
        this.value = value;
    }
}
