package com.interviewforge.experience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interviewforge.preparation.SkillRepository;
import com.interviewforge.questionbank.ResourceNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

class ExperienceStoryServiceTest {
    @Test
    void anotherUsersStoryIsNotFound() {
        UUID requestingUser = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();
        ExperienceStoryRepository repository = proxy(ExperienceStoryRepository.class,
                (method, args) -> method.getName().equals("findByIdAndUserId") ? Optional.empty() : unsupported(method));
        ExperienceStoryService service = new ExperienceStoryService(repository, unusedSkills());

        assertThrows(ResourceNotFoundException.class, () -> service.get(requestingUser, storyId));
    }

    @Test
    void deletionLooksUpStoryWithOwnerBeforeDeleting() {
        UUID requestingUser = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();
        ExperienceStory story = new ExperienceStory(requestingUser, "Example", ExperienceType.PROJECT,
                null, "Situation", "Task", "Actions", "Result", null, Set.of());
        UUID[] ownerUsedForLookup = new UUID[1];
        ExperienceStory[] deleted = new ExperienceStory[1];
        ExperienceStoryRepository repository = proxy(ExperienceStoryRepository.class, (method, args) -> {
            if (method.getName().equals("findByIdAndUserId")) {
                ownerUsedForLookup[0] = (UUID) args[1];
                return Optional.of(story);
            }
            if (method.getName().equals("delete")) {
                deleted[0] = (ExperienceStory) args[0];
                return null;
            }
            return unsupported(method);
        });
        ExperienceStoryService service = new ExperienceStoryService(repository, unusedSkills());

        service.delete(requestingUser, storyId);

        assertEquals(requestingUser, ownerUsedForLookup[0]);
        assertEquals(story, deleted[0]);
    }

    private static SkillRepository unusedSkills() {
        return proxy(SkillRepository.class, (method, args) -> unsupported(method));
    }

    private static Object unsupported(Method method) {
        throw new UnsupportedOperationException("Unexpected repository call: " + method.getName());
    }

    private static <T> T proxy(Class<T> type, BiFunction<Method, Object[], Object> handler) {
        Object instance = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (proxy, method, args) -> handler.apply(method, args));
        return type.cast(instance);
    }
}
