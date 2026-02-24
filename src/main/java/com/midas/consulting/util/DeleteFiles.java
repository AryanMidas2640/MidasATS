package com.midas.consulting.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DeleteFiles {

    public static void main(String[] args) throws IOException {
        List<String> files= Files.readAllLines(Paths.get("C:\\Users\\dell\\Desktop\\deleted.txt"));

        System.out.println(files);

        files.stream().forEach(x->{
            try {
                Files.delete(Paths.get("C:\\Users\\dell\\Downloads\\CVs - 100000\\another instance\\"+x));

                System.out.println("Deleted" + x);
            } catch (IOException e) {
                System.err.println(e.getCause());
            }
        });
    }
}
