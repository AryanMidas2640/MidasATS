

package com.midas.consulting.controller.v1.api;

import com.midas.consulting.model.Note;
import com.midas.consulting.service.NoteService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


@EnableCaching
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {
    @Autowired
    private NoteService noteService;

    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})

    @PostMapping
    public Note createNote(Principal principal, @RequestBody Note note, @RequestParam String userId) {
        return noteService.createNoteWithUser(note, userId);
    }

    @GetMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public List<Note> getAllNotes(Principal principal) {
        return noteService.getAllNotes();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Optional<Note> getNoteById(Principal principal,@PathVariable String id) {
        return noteService.getNoteById(id);
    }

    @GetMapping("/linkToId/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Optional<Note> getNoteBylinkToId(Principal principal,@PathVariable String id) {
        return noteService.getNoteByLinkToId(id);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Note updateNote(Principal principal,@PathVariable String id, @RequestBody Note noteDetails) {
        return noteService.updateNote(id, noteDetails);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public void deleteNoteById(Principal principal,@PathVariable String id) {
        noteService.deleteNoteById(id);
    }

//    ViewNoteBySourceId

    @GetMapping("/ViewNoteBySourceId/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public List<Note> viewNoteBySourceId(Principal principal,@PathVariable String id) {
        return noteService.viewNoteBySourceId(id);
    }

    @GetMapping("/ViewNoteByCandidateId/{candidateId}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public List<Note> viewNoteByCandidateId(Principal principal,@PathVariable String candidateId) {
        return noteService.viewNoteByCandidateId(candidateId);
    }
    @PostMapping("/{originalNoteId}/linked")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Note addLinkedNote(Principal principal,@PathVariable String originalNoteId, @RequestBody Note newLinkedNote, @RequestParam String userId) {
        return noteService.addLinkedNoteToOriginal(originalNoteId, newLinkedNote, userId);
    }
}
