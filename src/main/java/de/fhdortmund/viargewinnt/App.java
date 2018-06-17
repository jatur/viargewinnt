package de.fhdortmund.viargewinnt;

import static spark.Spark.get;

public class App {
    public static void main(String[] args) {
        get("/hello", (req, res) -> {

            return "hi";
        });
    }
}
