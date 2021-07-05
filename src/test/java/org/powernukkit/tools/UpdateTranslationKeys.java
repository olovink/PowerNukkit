/*
 * https://PowerNukkit.org - The Nukkit you know but Powerful!
 * Copyright (C) 2020  José Roberto de Araújo Júnior
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.powernukkit.tools;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.var;
import org.powernukkit.version.Version;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author joserobjr
 * @since 2021-07-04
 */
public class UpdateTranslationKeys {
    private static final Set<String> BLACK_LIST = new HashSet<>(Arrays.asList(
            //"accessibility", "eula"
    ));

    public static void main(@Nonnull String[] args) throws IOException {
        var updatedLangFile = readLangFile(Paths.get(args[0]));
        var structure = readEnumEntries();
        var updatedEntries = buildUpdatedEntries(structure.entries, updatedLangFile);
        var remappedEntries = remapNullGroup(updatedEntries);
        updateStructure(structure, remappedEntries);
        Files.write(structure.location, structure.lines);
        //structure.lines.forEach(System.out::println);
    }

    private static Map<String, Map<String, Match>> remapNullGroup(@Nonnull Map<String, Map<String, Match>> enumEntries) {
        var nullGroup = enumEntries.get(null);
        if (nullGroup == null) {
            return new LinkedHashMap<>(enumEntries);
        }

        Map<String, Map<String, Match>> remapped = nullGroup.values().stream().collect(Collectors.groupingBy(match -> {
            int index = match.propertiesKey.indexOf('_');
            index = match.propertiesKey.indexOf('_', index + 1);
            String baseKey = match.propertiesKey;
            if (index == -1 && baseKey.equals("gamepad_disconnect")) {
                return "gamepad_disconnect";
            }
            Preconditions.checkState(index >= 0);
            return baseKey.substring(0, index);
        }, Collectors.groupingBy(Match::getPropertiesKey, Collectors.reducing(null, (a, b) -> {
            if (a != null) throw new UnsupportedOperationException();
            return b.withEnumKey(createEnumKey(b.propertiesKey.substring(b.propertiesKey.indexOf('_') + 1)));
        }))));

        return Stream.concat(
                enumEntries.entrySet().stream().filter(it -> it.getKey() != null),
                remapped.entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
            var merge = new LinkedHashMap<String, Match>(a.size() + b.size());
            merge.putAll(a);
            merge.putAll(b);
            return merge;
        }, LinkedHashMap::new));
    }

    private static void updateStructure(@Nonnull JavaFileStructure fileStructure, @Nonnull Map<String, Map<String, Match>> enumEntries) {
        String defTab = "    ";
        String entryTab = "        ";
        List<String> newSource = new ArrayList<>(fileStructure.enumArea.size());
        StringBuilder sb = new StringBuilder();
        VersionComparator versionComparator = new VersionComparator();
        for (Map.Entry<String, Map<String, Match>> enums : enumEntries.entrySet()) {
            sb.setLength(0);
            final String enumName = createEnumName(enums.getKey(), sb);

            sb.setLength(0);
            sb.append(defTab);
            if (enums.getValue().values().stream().map(Match::getDeprecated).allMatch(Objects::nonNull)) {
                sb.append("@Deprecated @DeprecationDetails(since=\"");
                sb.append(enums.getValue().values().stream().map(Match::getDeprecated).min(versionComparator).orElse("FUTURE"))
                        .append("\", by=\"PowerNukkit Automation\", reason=\"Removed by Mojang\") ");
            }

            final String enumSince = enums.getValue().values().stream()
                    .map(Match::getSince).filter(it -> !"FUTURE" .equals(it)).max(versionComparator).orElse("FUTURE");
            sb.append("@PowerNukkitOnly @Since(\"").append(enumSince).append("\")");
            newSource.add(sb.toString());

            sb.setLength(0);
            sb.append(defTab).append("enum ").append(enumName).append(" implements TranslationKey {");
            newSource.add(sb.toString());

            for (Map.Entry<String, Match> enumEntry : enums.getValue().entrySet()) {
                sb.setLength(0);
                sb.append(entryTab).append("@PowerNukkitOnly ");
                Match match = enumEntry.getValue();
                if (match.deprecated != null) {
                    sb.append("@Deprecated @DeprecationDetails(since=\"").append(match.deprecated)
                            .append("\", by=\"PowerNukkit Automation\", reason=\"Removed by Mojang\") ");
                }
                sb.append("@Since(\"").append(match.since).append("\") ")
                        .append(match.enumKey)
                        .append("(\"").append(match.propertiesKey)
                        .append("\"), // ").append(match.value);
                newSource.add(sb.toString());
            }

            sb.setLength(0);
            sb.append(entryTab).append(';');
            newSource.add(sb.toString());

            sb.setLength(0);
            sb.append(entryTab).append("private final String code; ")
                    .append(enumName).append("(final String code) {").append("this.code = code;").append('}');
            newSource.add(sb.toString());

            sb.setLength(0);
            sb.append(entryTab).append("@PowerNukkitOnly @Since(\"").append(enumSince)
                    .append("\") @Nonnull @Override public String getCode() { return this.code; }");
            newSource.add(sb.toString());

            sb.setLength(0);
            sb.append(entryTab).append("@Override @Nonnull public String toString() { return \"%\" + getCode(); }");
            newSource.add(sb.toString());

            sb.setLength(0);
            sb.append(defTab).append('}');
            newSource.add(sb.toString());
            newSource.add("");
        }
        fileStructure.enumArea.clear();
        fileStructure.enumArea.addAll(newSource);
    }

    private static class VersionComparator implements Comparator<String> {
        @Override
        public int compare(@Nullable String a, @Nullable String b) {
            if (Objects.equals(a, b)) {
                return 0;
            } else if (a != null && b == null) {
                return 1;
            } else if (a == null) {
                return -1;
            } else if (a.equals("FUTURE")) {
                return -1;
            } else if (b.equals("FUTURE")) {
                return 1;
            } else {
                return (new Version(a)).compareTo(new Version(b));
            }
        }
    }

    @Nonnull
    private static Map<String, Map<String, Match>> buildUpdatedEntries(
            Map<String, Map<String, Match>> existing,
            Map<String, Map<String, Match>> updatedLang
    ) {
        //TODO The update logic
        return new LinkedHashMap<>(updatedLang);
    }

    @Nonnull
    private static JavaFileStructure readEnumEntries() throws IOException {
        Path location = Paths.get("src/main/java/cn/nukkit/lang/TranslationKey.java");
        List<String> keysSource = Files.readAllLines(location);
        if (!(keysSource instanceof ArrayList)) {
            keysSource = new ArrayList<>(keysSource);
        }
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0, keysSourceSize = keysSource.size(); i < keysSourceSize; i++) {
            String line = keysSource.get(i);
            if (startIndex == -1 && line.contains("//<editor-fold desc=\"enum entries\" defaultstate=\"collapsed\">")) {
                startIndex = i;
            } else if (startIndex > -1 && line.contains("//</editor-fold>")) {
                endIndex = i;
                break;
            }
        }
        if (endIndex < 0) throw new IllegalStateException("Start-end not found");

        List<String> enumArea = keysSource.subList(startIndex + 1, endIndex);

        Pattern sourceCodePattern = Pattern.compile("(?:@DeprecationDetais\\(since=\"([^\"]+)\".+?)?@Since\\(\"([^\"]+)\"\\)\\s([A-Z_]+)\\(\"([^\"]+)\"\\),?\\s*(?://\\s*([^\r\n]+))?");
        return new JavaFileStructure(location, keysSource, enumArea, startIndex, endIndex,
                enumArea.stream()
                        .map(sourceCodePattern::matcher)
                        .filter(Matcher::find)
                        .filter(it -> !"gamepad_disconnect" .equals(it.group(4)))
                        .map(match -> new SimpleEntry<>(
                                match.group(4).substring(0, match.group(4).indexOf('.')),
                                new Match(
                                        match.group(3),
                                        match.group(4),
                                        match.group(5),
                                        match.group(2),
                                        match.group(1)
                                )
                        )).collect(toComplexMap())
        );
    }

    @Nonnull
    private static Map<String, Map<String, Match>> readLangFile(@Nonnull Path file) throws IOException {
        Pattern pattern = Pattern.compile("^((?:([a-zA-Z0-9_]+)\\.)?([a-zA-Z0-9._:-]+))=(.+)");
        try (Stream<String> lines = Files.lines(file)) {
            return lines.map(pattern::matcher)
                    .filter(Matcher::find)
                    .filter(match -> match.group(2) == null || !BLACK_LIST.contains(match.group(2).toLowerCase(Locale.ENGLISH)))
                    .map(match -> new SimpleEntry<>(
                            match.group(2),
                            new Match(
                                    createEnumKey(match.group(3)),
                                    match.group(1),
                                    match.group(4)
                            )
                    )).collect(toComplexMap());
        }
    }

    @Nonnull
    private static String createEnumName(@Nonnull String name, @Nonnull StringBuilder sb) {
        final char firstChar = name.charAt(0);
        if (Character.isDigit(firstChar)) {
            String digitName = digitName(firstChar);
            sb.append(digitName.charAt(0));
            for (int i = 1, len = digitName.length(); i < len; i++) {
                sb.append(Character.toLowerCase(digitName.charAt(i)));
            }
        } else {
            sb.append(Character.toUpperCase(firstChar));
        }
        boolean nextIsUppercase = false;
        for (int i = 1, len = name.length(); i < len; i++) {
            char c = name.charAt(i);
            if (nextIsUppercase) {
                c = Character.toUpperCase(c);
                nextIsUppercase = false;
            } else if (c == '_') {
                nextIsUppercase = true;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Nonnull
    private static String createEnumKey(@Nonnull String name) {
        if (name.equals("renderclouds")) {
            return "RENDERCLOUDS_LOWER_CASED";
        }

        String key = name.toUpperCase(Locale.ENGLISH)
                .replaceAll("[^A-Z0-9_]", "_")
                .replaceAll("_{2,}", "_");

        if (Character.isDigit(key.charAt(0))) {
            key = digitName(key.charAt(0)) + key.substring(1);
        }

        return key;
    }

    @Nonnull
    private static String digitName(char digit) {
        switch (digit) {
            case '0':
                return "ZERO_";
            case '1':
                return "ONE_";
            case '2':
                return "TWO_";
            case '3':
                return "THREE_";
            case '4':
                return "FOUR_";
            case '5':
                return "FIVE_";
            case '6':
                return "SIX_";
            case '7':
                return "SEVEN_";
            case '8':
                return "EIGHT_";
            case '9':
                return "NINE_";
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Nonnull
    private static Collector<Map.Entry<String, Match>, ?, Map<String, Map<String, Match>>> toComplexMap() {
        return Collectors.toMap(
                Map.Entry::getKey,
                v -> Collections.singletonMap(v.getValue().enumKey, v.getValue()),
                (a, b) -> {
                    Map<String, Match> combined = new LinkedHashMap<>(a.size() + b.size());
                    combined.putAll(a);
                    combined.putAll(b);
                    Preconditions.checkState(combined.size() == a.size() + b.size());
                    return combined;
                },
                LinkedHashMap::new
        );
    }

    @Value
    private static class JavaFileStructure {
        @Nonnull
        Path location;
        @Nonnull
        List<String> lines;
        @Nonnull
        List<String> enumArea;
        int startIndex;
        int endIndex;
        @Nonnull
        Map<String, Map<String, Match>> entries;
    }

    @With
    @AllArgsConstructor
    @Value
    private static class Match {
        @Nonnull
        String enumKey;
        @Nonnull
        String propertiesKey;
        @Nullable
        String value;
        @Nonnull
        String since;
        @Nullable
        String deprecated;

        public Match(@Nonnull String enumKey, @Nonnull String propertiesKey, @Nullable String value) {
            this.enumKey = enumKey;
            this.propertiesKey = propertiesKey;
            this.value = value;
            this.since = "FUTURE";
            this.deprecated = null;
        }
    }
}
