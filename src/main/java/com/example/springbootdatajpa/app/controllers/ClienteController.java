package com.example.springbootdatajpa.app.controllers;

import com.example.springbootdatajpa.app.models.entity.Cliente;
import com.example.springbootdatajpa.app.service.IClienteService;
import com.example.springbootdatajpa.app.util.paginator.PageRender;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

    @Autowired
    private IClienteService clienteService;

    private final static String UPLOADS_FOLDER = "uploads";
    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping(value = "/upload/{filename:,+}")
    public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
        Path pathFoto = Paths.get(UPLOADS_FOLDER).resolve(filename).toAbsolutePath();
        log.info("pathFoto: " + pathFoto);
        Resource recurso = null;
        try {
            recurso = new UrlResource(pathFoto.toUri());
            if (!recurso.exists() && !recurso.isReadable()){
                throw new RuntimeException("Error: no se puede cargar la imagen" + pathFoto.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                + recurso.getFilename()+"\"").body(recurso);
    }

    @GetMapping(value = "/ver/{id}")
    public String ver(@PathVariable(value = "id")Long id, Map<String, Object> model, RedirectAttributes flash){
        Cliente cliente = clienteService.findOne(id);
        if (cliente==null){
            flash.addFlashAttribute("error", "El cliente no Existe");
            return "redirect:/listar";
        }
        model.put("cliente", cliente);
        model.put("titulo", "Detalle Cliente: " + cliente.getNombre());
        return "ver";
    }

    @RequestMapping(value = "/listar", method = RequestMethod.GET)
    public String listar(@RequestParam(name = "page", defaultValue = "0")int page, Model model){

        Pageable pageRequest = PageRequest.of(page, 4);
        Page<Cliente> clientes = clienteService.findAll(pageRequest);

        PageRender<Cliente> pageRender = new PageRender<Cliente>("/listar", clientes);
        model.addAttribute("titulo", "Listado de  Clientes");
        model.addAttribute("clientes", clientes);
        model.addAttribute("page", pageRender);
        return "listar";
    }

    @RequestMapping(value = "/form")
    public String crear(Map<String, Object> model){
        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo", "Formulario de Cliente");
        return "form";
    }

    //Buscar propiedad especifica= {...}
    @RequestMapping(value = "/form/{id}")
    public String editar(@PathVariable(value = "id")Long id, Map<String, Object> model, RedirectAttributes flash){
        Cliente cliente = null;
        if (id>0){
            cliente = clienteService.findOne(id);
            if (cliente == null){
                flash.addFlashAttribute("error", "El ID del Cliente no existe en la DB");
                return "redirect:/listar";
            }
        } else {
            //validar la existencia del CLinte con flash.
            flash.addFlashAttribute("error", "El id del Cliente no puede ser cero");
            return "redirect:/listar";
        }
        model.put("cliente",cliente);
        model.put("titulo","Editar Cliente");
        return "form";
    }

    //method, metodo por default: get
    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String guardar(@Valid Cliente cliente, BindingResult result, Model model, @RequestParam("file")
                          MultipartFile foto, RedirectAttributes flash, SessionStatus status){
        if (result.hasErrors()){
            model.addAttribute("titulo", "Formulario de Cliente");
            return "form";
        }
        if (!foto.isEmpty()){
                if (cliente.getId()!=null && cliente.getId()>0 && cliente.getFoto()!= null
                        && cliente.getFoto().length() >0){
                    Path rootPath = Paths.get(UPLOADS_FOLDER).resolve(cliente.getFoto()).toAbsolutePath();
                    File archivo = rootPath.toFile();
                    if (archivo.exists() && archivo.canRead()){
                        archivo.delete();
                    }
                }
            String uniqueFilename = UUID.randomUUID().toString() + "-" + foto.getOriginalFilename();
            Path rootPath = Paths.get(UPLOADS_FOLDER).resolve(uniqueFilename);
            Path rootAbsolutePath = rootPath.toAbsolutePath();
            log.info("rootPath" + rootPath);
            log.info("rootAbsolutePath" + rootAbsolutePath);
            try {
                Files.copy(foto.getInputStream(),rootAbsolutePath);
                flash.addFlashAttribute("info", "Se ha subido correctamente "
                        + uniqueFilename + "'");
                cliente.setFoto(uniqueFilename);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito" : "Cliente creado con éxito";

        clienteService.save(cliente);
        status.setComplete();
        //valida el proceso de guardar un Cliente
        flash.addFlashAttribute("success", mensajeFlash);
        return "redirect:listar";
    }

    @RequestMapping(value = "/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id")Long id, RedirectAttributes flash){
        if (id>0){
            Cliente cliente = clienteService.findOne(id);
            clienteService.delete(id);
            //confirmar accion de un crud con flash.
            flash.addFlashAttribute("succes", "Cliente eliminado con éxito");
            Path rootPath = Paths.get(UPLOADS_FOLDER).resolve(cliente.getFoto()).toAbsolutePath();
            File archivo = rootPath.toFile();
            if (archivo.exists() && archivo.canRead()){
                if (archivo.delete()){
                    flash.addFlashAttribute("info", "Foto" + cliente.getFoto()
                    + "Se elimino la foto!");
                }
            }
        }
        return "redirect:/listar";
    }

}
