package ru.urasha.callmeani.dream_marketplace.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.urasha.callmeani.dream_marketplace.models.entities.Category;
import ru.urasha.callmeani.dream_marketplace.repositories.CategoryRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class CategorySeeder {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void seed() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<String> defaults = Arrays.asList(
                "Фантастика",
                "Научная фантастика",
                "Фэнтези",
                "Хоррор",
                "Детектив",
                "Путешествия",
                "Романтика",
                "Киберпанк",
                "Космос",
                "История",
                "Мифология",
                "Мистика",
                "Приключения",
                "Юмор",
                "Постапокалипсис",
                "Сюрреализм",
                "Повседневность",
                "Природа",
                "Абстракция",
                "Психология"
        );

        defaults.forEach(name -> {
            if (!categoryRepository.existsByName(name)) {
                Category c = new Category();
                c.setName(name);
                categoryRepository.save(c);
            }
        });
    }
}
