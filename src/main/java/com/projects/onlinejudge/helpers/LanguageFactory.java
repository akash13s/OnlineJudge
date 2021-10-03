package com.projects.onlinejudge.helpers;

import com.projects.onlinejudge.models.Lang;
import org.springframework.stereotype.Component;

@Component
public class LanguageFactory {
    public IRunner getLangRunner(String language){
        if(language.equals(Lang.CPP.getValue())){
            IRunner runner = new CPPRunner();
            return runner;
        } else if(language.equals(Lang.JAVA.getValue())){
            IRunner runner = new JavaRunner();
            return runner;
        } else if(language.equals(Lang.PYTHON.getValue())){
            IRunner runner = new PythonRunner();
            return runner;
        }
        return null;
    }
}
