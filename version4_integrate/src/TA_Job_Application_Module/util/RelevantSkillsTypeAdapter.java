package TA_Job_Application_Module.util;

import TA_Job_Application_Module.model.Application;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Reads legacy string arrays and structured {name, proficiency} objects. */
public class RelevantSkillsTypeAdapter extends TypeAdapter<List<Application.RelevantSkill>> {

    @Override
    public void write(JsonWriter out, List<Application.RelevantSkill> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginArray();
        for (Application.RelevantSkill sk : value) {
            if (sk == null || sk.getName() == null || sk.getName().isBlank()) {
                continue;
            }
            out.beginObject();
            out.name("name").value(sk.getName());
            if (sk.getProficiency() != null && !sk.getProficiency().isBlank()) {
                out.name("proficiency").value(sk.getProficiency());
            }
            out.endObject();
        }
        out.endArray();
    }

    @Override
    public List<Application.RelevantSkill> read(JsonReader in) throws IOException {
        List<Application.RelevantSkill> result = new ArrayList<>();
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return result;
        }
        in.beginArray();
        while (in.hasNext()) {
            JsonToken token = in.peek();
            if (token == JsonToken.STRING) {
                result.add(RelevantSkillsHelper.parseSingle(in.nextString()));
            } else if (token == JsonToken.BEGIN_OBJECT) {
                in.beginObject();
                String name = null;
                String proficiency = null;
                while (in.hasNext()) {
                    String field = in.nextName();
                    if ("name".equals(field)) {
                        name = in.nextString();
                    } else if ("proficiency".equals(field)) {
                        proficiency = in.nextString();
                    } else {
                        in.skipValue();
                    }
                }
                in.endObject();
                result.add(new Application.RelevantSkill(name, proficiency));
            } else {
                in.skipValue();
            }
        }
        in.endArray();
        return result;
    }
}
