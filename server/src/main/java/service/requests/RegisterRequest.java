package service.requests;

import com.google.gson.JsonElement;
import model.UserData;

public record RegisterRequest(String username, String password, String email) {}
