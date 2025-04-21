package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.User;
import com.financeiro.api.dto.userDTO.UserRequestDTO;
import com.financeiro.api.dto.userDTO.UserResponseDTO;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.domain.template.CategoryTemplate;
import com.financeiro.api.domain.template.SubcategoryTemplate;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.repository.template.CategoryTemplateRepository;
import com.financeiro.api.repository.template.SubcategoryTemplateRepository;
import com.financeiro.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryTemplateRepository categoryTemplateRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired SubcategoryRepository subcategoryRepository;

    @Autowired
    SubcategoryTemplateRepository subcategoryTemplateRepository;

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setStatus(Status.SIM);
        user.setCreatedAt(LocalDateTime.now());

        User saved = repository.save(user);

    try {
        System.out.println("🧹 Clonando categorias e subcategorias do template...");

        List<CategoryTemplate> templates = categoryTemplateRepository.findAll();

        for (CategoryTemplate template : templates) {
            Category category = new Category();
            category.setUser(saved);
            category.setName(template.getName());
            category.setColor(template.getColor());
            category.setIconClass(template.getIconClass());
            category.setType(template.getType());
            category.setStatus(Status.SIM);
            category.setAdditionalInfo(template.getAdditionalInfo());
            category.setStandardRecommendation(template.isStandardRecommendation());
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());

            Category savedCategory = categoryRepository.save(category);

            List<SubcategoryTemplate> subTemplates = subcategoryTemplateRepository.findAll()
                    .stream()
                    .filter(s -> s.getCategoryTemplate().getId().equals(template.getId()))
                    .toList();

            for (SubcategoryTemplate sub : subTemplates) {
                Subcategory subcategory = new Subcategory();
                subcategory.setCategory(savedCategory);
                subcategory.setName(sub.getName());
                subcategory.setColor(sub.getColor());
                subcategory.setIconClass(sub.getIconClass());
                subcategory.setStatus(Status.SIM);
                subcategory.setAdditionalInfo(sub.getAdditionalInfo());
                subcategory.setStandardRecommendation(true);
                subcategory.setCreatedAt(LocalDateTime.now());
                subcategory.setUpdatedAt(LocalDateTime.now());

                subcategoryRepository.save(subcategory);
            }
        }

        System.out.println("🎉 Templates clonados com sucesso!");
    } catch (Exception e) {
        System.out.println("❌ Erro ao clonar templates: " + e.getMessage());
        e.printStackTrace();
    }

        return toDTO(saved);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(
                        () -> new UserNotFoundException()
                );
    }

    @Override
    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        User user = repository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        user.setName(dto.name());
        user.setEmail(dto.email());
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        user.setUpdatedAt(LocalDateTime.now());

        return toDTO(repository.save(user));
    }

    @Override
    public void delete(UUID id) {
        User user = repository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        user.setStatus(Status.EXC);
        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
    }

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getStatus()
        );
    }
}
