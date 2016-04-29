package org.ranran.tomcatredissessionmanager.exampleapp;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Collections;
import spark.ResponseTransformerRoute;
import spark.Session;
import javax.servlet.http.HttpSession;

import org.ranran.tomcat.redissessions.RedisSession;

public abstract class JsonTransformerRoute extends ResponseTransformerRoute {

    private Gson gson = new Gson();

    protected JsonTransformerRoute(String path) {
      super(path);
    }

    protected JsonTransformerRoute(String path, String acceptType) {
      super(path, acceptType);
    }

    @Override
    public String render(Object jsonObject) {
      return gson.toJson(jsonObject);
    }

}