package com.emarte.oojaba.data;

import java.util.HashMap;
import java.util.Map;

import static com.emarte.oojaba.data.EndPoint.StandardAttributes.*;

public enum EndPointType {
    RESTFUL {
        @Override
        public Map<String, String> deriveAttributes(String text) {
            Map<String, String> attributes = new HashMap<>();

                if(text.contains(" ")) {
                    String part1 = text.substring(0, text.indexOf(" "));

                    if(part1.toUpperCase().equals(part1)) {
                        attributes.put(METHOD.value, part1);
                        text = text.substring(text.indexOf(" ") + 1);
                    }
                }

                if(text.contains(" ")) {
                    String part2 = text.substring(text.lastIndexOf(" ") + 1);

                    if(part2.contains("/")) {
                        attributes.put(PATH.value, part2);
                        text = text.substring(0, text.lastIndexOf(" "));
                    }
                }

            attributes.put(DESC.value, text);
            return attributes;
        }

        @Override
        public String deriveText(Map<String, String> attributes) {
            StringBuilder builder = new StringBuilder();

            if(attributes.containsKey(METHOD.value)) {
                builder.append(attributes.get(METHOD.value)).append(" ");
            }

            builder.append(attributes.get(DESC.value));

            if(attributes.containsKey(PATH.value)) {
                builder.append(" ").append(attributes.get(PATH.value));
            }

            return builder.toString();
        }

        @Override
        public String getExplanationText() {
            return "[METHOD] Textual description [/path/]";
        }
    },
    QUEUE,
    TOPIC,
    LISTENER,
    REMOTE_STORAGE;

    public Map<String, String> deriveAttributes(String text) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(DESC.value, text);
        return attributes;
    }

    public String deriveText(Map<String, String> attributes) {
        return attributes.get(DESC.value);
    }

    public String getExplanationText() {
        return "Textual description";
    }
}