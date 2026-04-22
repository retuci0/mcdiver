# retucio's Minecraft diver

> **dive into and play around with the Minecraft source**: browse fields, call methods, ... all ingame

## keybinds

<table>
    <tr>
        <td>open gui</td>
        <td>double tap right shift</td>
    </tr>
    <tr>
        <td>close gui</td>
        <td>esc.</td>
    </tr>
    <tr>
        <td>autocomplete</td>
        <td>tab</td>
    </tr>
</table>

## macros

type `$macro` in the text field followed by:
- `set <name> <value>` to add a macro
- `del <name>` to remove a macro
- `bind <name> <key>` to bind a key to a macro (see next section for obtaining key names)
- `unbind <name>` to unbind a key from a macro
- `list` to list all macros
- `run <name>` to run a macro, or directly type `$<name>`

## changing bound key

type `$bind <key>` into the text field. 

obtain the key name by removing the `GLFW_KEY_` from [this table's](https://www.glfw.org/docs/latest/group__keys.html) keys

## how?

via reflection

## why?

because debugging can be fun

## license

whatever.