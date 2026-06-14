package me.hhitt.disasters.command;

import org.junit.jupiter.api.Test;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandPlaceholderBindingTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("<([A-Za-z][A-Za-z0-9_-]*)>");

    @Test
    void allCurrentCommandPlaceholdersMatchRetainedParameterNames() {
        final List<String> mismatches = new ArrayList<String>();
        validate(ArenaCommand.class, mismatches);
        validate(DisastersCommand.class, mismatches);

        assertTrue(mismatches.isEmpty(), String.join(System.lineSeparator(), mismatches));
    }

    private static void validate(final Class<?> commandClass, final List<String> mismatches) {
        for (final Method method : commandClass.getDeclaredMethods()) {
            final Set<String> parameterNames = new HashSet<String>();
            for (final Parameter parameter : method.getParameters()) {
                parameterNames.add(parameter.getName());
            }

            for (final String path : commandPaths(method)) {
                final Matcher matcher = PLACEHOLDER.matcher(path);
                while (matcher.find()) {
                    final String placeholder = matcher.group(1);
                    if (!parameterNames.contains(placeholder)) {
                        mismatches.add(
                            method.toGenericString()
                                + " path='" + path + "' missing=<" + placeholder + ">"
                                + " available=" + parameterNames
                        );
                    }
                }
            }
        }
    }

    private static List<String> commandPaths(final Method method) {
        final List<String> paths = new ArrayList<String>();
        final Command command = method.getAnnotation(Command.class);
        if (command != null) {
            paths.addAll(Arrays.asList(command.value()));
        }
        final Subcommand subcommand = method.getAnnotation(Subcommand.class);
        if (subcommand != null) {
            paths.addAll(Arrays.asList(subcommand.value()));
        }
        return paths;
    }
}
