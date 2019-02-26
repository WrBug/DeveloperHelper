package com.github.megatronking.netbare.injector;

import androidx.annotation.NonNull;
import com.github.megatronking.netbare.http.HttpRequest;
import com.github.megatronking.netbare.http.HttpResponse;

import java.util.Map;

public class AppHttpInjector extends SimpleHttpInjector {
    @Override
    public boolean sniffResponse(@NonNull HttpResponse response) {
        return super.sniffResponse(response);
    }
}
