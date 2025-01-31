package org.keycloak.test.migration;

import java.util.List;

public abstract class Migrator {

    protected List<String> content;

    public abstract void rewrite();

    protected int findLine(String regex) {
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).matches(regex)) {
                return i;
            }
        }
        return -1;
    }

    protected int findClassDeclaration() {
        return findLine("public class .*");
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public void replaceLine(int line, String updated) {
        content.remove(line);
        content.add(line, updated);
    }

    protected void info(int line, String message) {
        System.out.println(String.format("%5s", line) + " " + message);
    }

}
