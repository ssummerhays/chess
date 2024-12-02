package service.requests;

public record UpdateGameRequest(String authToken, String jsonGameData) {
}
