package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.RecursoDTO;
import com.example.demo.service.RecursoService;

@RestController
@RequestMapping("/recursos")
public class RecursoController extends BaseController<RecursoDTO> {

    public RecursoController(RecursoService service){
        super(service);
    }

}
