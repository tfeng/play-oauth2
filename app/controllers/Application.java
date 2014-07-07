package controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

@Service
public class Application extends Controller {

  @PreAuthorize("hasRole('ROLE_USER')")
  public Promise<Result> userDetails() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    ObjectNode json = Json.newObject();
    json.setAll(ImmutableMap.of(
        "username", json.textNode(username), "isActive", json.booleanNode(true)));
    return Promise.pure(ok(json));
  }
}
