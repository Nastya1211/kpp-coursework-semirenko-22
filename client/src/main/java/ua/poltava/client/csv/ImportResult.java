package ua.poltava.client.csv;

import java.util.List;

public record ImportResult(int imported, List<String> errors) {}