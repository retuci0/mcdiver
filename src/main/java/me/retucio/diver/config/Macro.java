package me.retucio.diver.config;


public class Macro {

    private String name;
    private String command;
    private int keybind = -1;

    public Macro() {}  // for JSON deserialization

    public Macro(String name, String command) {
        this.name = name;
        this.command = command;
    }

    public String getName() { return name; }
    public String getCommand() { return command; }
    public int getKeybind() { return keybind; }

    public void setName(String name) { this.name = name; }
    public void setCommand(String command) { this.command = command; }
    public void setKeybind(int keybind) { this.keybind = keybind; }

}