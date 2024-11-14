package service.results;

import model.PrintedGameData;

import java.util.Collection;

public record ListGamesResult(Collection<PrintedGameData> games) {}
