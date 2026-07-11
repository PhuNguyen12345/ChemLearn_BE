package com.example.chemlearn.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequiredProductionDataSeeder {

    private static final String BUILD_MARKER = "required-seed-v2-2026-06-15";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.required-seed.enabled:true}")
    private boolean enabled;

    @Value("${app.required-seed.replace-premade-labs:true}")
    private boolean replacePremadeLabs;

    @Value("${app.seed-admin.username:duckhisuu}")
    private String adminUsername;

    @Value("${app.seed-admin.password:Hieubeep1407@}")
    private String adminPassword;

    @Value("${app.seed-admin.email:duckhisuu@chemlearn.local}")
    private String adminEmail;

    @Value("${app.seed-admin.full-name:Duck Hisuu}")
    private String adminFullName;

    @Value("${app.seed-admin.sync-password:true}")
    private boolean syncAdminPassword;

    public static String buildMarker() {
        return BUILD_MARKER;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedAfterStartup() {
        if (!enabled) {
            log.info("Required production data seed is disabled.");
            return;
        }

        runStage("admin", this::seedAdmin);
        runStage("inventory", this::seedInventoryItems);
        runStage("virtual-labs", () -> {
            if (replacePremadeLabs) {
                replacePremadeLabsWithRequiredCatalog();
            } else {
                seedRequiredLabs(false);
            }
        });
        log.info("Required production data seed completed: {}", BUILD_MARKER);
    }

    private void runStage(String stage, Runnable action) {
        try {
            action.run();
            log.info("Required seed stage '{}' completed.", stage);
        } catch (Exception ex) {
            log.error("Required seed stage '{}' failed: {}", stage, ex.getMessage(), ex);
        }
    }

    private void seedAdmin() {
        String encodedPassword = passwordEncoder.encode(adminPassword);
        int updated = jdbcTemplate.update("""
                UPDATE users
                SET email = ?,
                    full_name = ?,
                    role = 'ROLE_ADMIN',
                    is_active = TRUE,
                    auth_provider = 'LOCAL',
                    failed_login_attempts = 0,
                    lockout_until = NULL,
                    last_failed_at = NULL,
                    password = CASE WHEN ? THEN ? ELSE password END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE username = ?
                """,
                adminEmail,
                adminFullName,
                syncAdminPassword,
                encodedPassword,
                adminUsername
        );

        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO users (
                        username,
                        email,
                        password,
                        full_name,
                        role,
                        is_active,
                        auth_provider,
                        failed_login_attempts,
                        lockout_until,
                        last_failed_at,
                        created_at,
                        updated_at
                    )
                    VALUES (?, ?, ?, ?, 'ROLE_ADMIN', TRUE, 'LOCAL', 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    adminUsername,
                    adminEmail,
                    encodedPassword,
                    adminFullName
            );
        }
    }

    private void seedInventoryItems() {
        List<InventorySeed> items = List.of(
                new InventorySeed("beaker", "Cốc thủy tinh", "CONTAINER", null, null, "Beaker", "text-cyan-500", "#e0f2fe"),
                new InventorySeed("test_tube", "Ống nghiệm", "CONTAINER", null, null, "TestTube", "text-violet-500", "#ede9fe"),
                new InventorySeed("bunsen_burner", "Đèn Bunsen", "EQUIPMENT", null, null, "Flame", "text-orange-500", "#fed7aa"),
                new InventorySeed("zn_grain", "Kẽm hạt", "CHEMICAL", "SOLID", "METAL", "CircleDot", "text-slate-500", "#cbd5e1"),
                new InventorySeed("sodium", "Natri", "CHEMICAL", "SOLID", "METAL", "Square", "text-slate-400", "#e2e8f0"),
                new InventorySeed("water", "Nước", "CHEMICAL", "LIQUID", "SOLVENT", "Droplet", "text-sky-500", "#bae6fd"),
                new InventorySeed("hcl", "Axit HCl", "CHEMICAL", "LIQUID", "ACID", "Droplet", "text-red-500", "#fecaca"),
                new InventorySeed("naoh_sol", "Dung dịch NaOH", "CHEMICAL", "LIQUID", "ALKALI", "Droplet", "text-blue-500", "#bfdbfe"),
                new InventorySeed("phenolphthalein", "Phenolphthalein", "CHEMICAL", "LIQUID", "INDICATOR", "Droplet", "text-pink-500", "#fbcfe8"),
                new InventorySeed("litmus_paper", "Giấy quỳ tím", "CHEMICAL", "SOLID", "INDICATOR", "Square", "text-purple-500", "#ddd6fe"),
                new InventorySeed("bacl2", "Dung dịch BaCl2", "CHEMICAL", "LIQUID", "SALT_SOLUTION", "Droplet", "text-sky-600", "#dbeafe"),
                new InventorySeed("na2so4", "Dung dịch Na2SO4", "CHEMICAL", "LIQUID", "SALT_SOLUTION", "Droplet", "text-indigo-500", "#e0e7ff"),
                new InventorySeed("agno3", "Dung dịch AgNO3", "CHEMICAL", "LIQUID", "SALT_SOLUTION", "Droplet", "text-gray-500", "#f3f4f6"),
                new InventorySeed("nacl", "Natri clorua", "CHEMICAL", "SOLID", "SALT_SOLID", "CircleDot", "text-slate-500", "#f8fafc")
        );

        for (InventorySeed item : items) {
            jdbcTemplate.update("""
                    INSERT INTO inventory_item (
                        item_code,
                        name,
                        type,
                        state,
                        description,
                        properties,
                        sub_category,
                        icon_name,
                        icon_color,
                        icon_fill
                    )
                    VALUES (?, ?, ?, ?, '', '{}'::jsonb, ?, ?, ?, ?)
                    ON CONFLICT (item_code) DO UPDATE
                    SET name = EXCLUDED.name,
                        type = EXCLUDED.type,
                        state = EXCLUDED.state,
                        sub_category = EXCLUDED.sub_category,
                        icon_name = EXCLUDED.icon_name,
                        icon_color = EXCLUDED.icon_color,
                        icon_fill = EXCLUDED.icon_fill
                    """,
                    item.itemCode(),
                    item.name(),
                    item.type(),
                    item.state(),
                    item.subCategory(),
                    item.iconName(),
                    item.iconColor(),
                    item.iconFill()
            );
        }
    }

    private void replacePremadeLabsWithRequiredCatalog() {
        jdbcTemplate.update("""
                DELETE FROM lab
                WHERE type = 'PREMADE'
                  AND title NOT IN (?, ?, ?, ?)
                """,
                "Điều chế khí Hidro",
                "Tính chất hóa học của Nước",
                "Phân loại chất bằng chất chỉ thị",
                "Phản ứng trao đổi trong dung dịch"
        );

        seedRequiredLabs(true);
    }

    private void seedRequiredLabs(boolean overwriteExisting) {
        UUID adminId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                UUID.class,
                adminUsername
        );

        List<LabSeed> labs = List.of(
                new LabSeed(
                        "Điều chế khí Hidro",
                        "Thực hành phản ứng giữa kim loại kẽm (Zn) và axit clohidric (HCl) để sinh ra khí Hidro.",
                        "KIM_LOAI",
                        "EASY",
                        0,
                        "[\"beaker\",\"test_tube\",\"bunsen_burner\",\"zn_grain\",\"hcl\"]"
                ),
                new LabSeed(
                        "Tính chất hóa học của Nước",
                        "Khảo sát phản ứng của Natri với nước và dùng Phenolphthalein để kiểm chứng dung dịch sinh ra có tính kiềm.",
                        "GENERAL",
                        "MEDIUM",
                        0,
                        "[\"beaker\",\"test_tube\",\"bunsen_burner\",\"sodium\",\"water\",\"phenolphthalein\"]"
                ),
                new LabSeed(
                        "Phân loại chất bằng chất chỉ thị",
                        "Dùng quỳ tím và Phenolphthalein để nhận biết môi trường axit HCl và bazơ NaOH.",
                        "AXIT_BAZO",
                        "EASY",
                        0,
                        "[\"beaker\",\"test_tube\",\"hcl\",\"naoh_sol\",\"phenolphthalein\",\"litmus_paper\"]"
                ),
                new LabSeed(
                        "Phản ứng trao đổi trong dung dịch",
                        "Quan sát phản ứng trao đổi tạo kết tủa đặc trưng với AgNO3 - NaCl và BaCl2 - Na2SO4.",
                        "KET_TUA",
                        "MEDIUM",
                        0,
                        "[\"beaker\",\"test_tube\",\"bacl2\",\"na2so4\",\"agno3\",\"nacl\"]"
                )
        );

        for (LabSeed lab : labs) {
            UUID labId = findLabId(lab.title());
            if (labId == null) {
                labId = jdbcTemplate.queryForObject("""
                        INSERT INTO lab (
                            title,
                            description,
                            category,
                            difficulty,
                            type,
                            max_score,
                            author_id,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, 'PREMADE', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        RETURNING id
                        """,
                        UUID.class,
                        lab.title(),
                        lab.description(),
                        lab.category(),
                        lab.difficulty(),
                        lab.maxScore(),
                        adminId
                );
            } else if (overwriteExisting) {
                jdbcTemplate.update("""
                        UPDATE lab
                        SET description = ?,
                            category = ?,
                            difficulty = ?,
                            type = 'PREMADE',
                            max_score = ?,
                            author_id = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        lab.description(),
                        lab.category(),
                        lab.difficulty(),
                        lab.maxScore(),
                        adminId,
                        labId
                );
            }

            upsertLabConfiguration(labId, lab.allowedChemicalsJson());
        }
    }

    private UUID findLabId(String title) {
        List<UUID> ids = jdbcTemplate.query(
                "SELECT id FROM lab WHERE title = ? AND type = 'PREMADE' ORDER BY created_at LIMIT 1",
                (rs, rowNum) -> (UUID) rs.getObject("id"),
                title
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void upsertLabConfiguration(UUID labId, String allowedChemicalsJson) {
        int updated = jdbcTemplate.update("""
                UPDATE lab_configuration
                SET config = jsonb_build_object('allowed_chemicals', CAST(? AS jsonb)),
                    viewport = '{"offset":{"x":0,"y":0},"zoom_scale":1.0}'::jsonb,
                    initial_workspace = '[]'::jsonb
                WHERE lab_id = ?
                """,
                allowedChemicalsJson,
                labId
        );

        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO lab_configuration (
                        lab_id,
                        config,
                        viewport,
                        initial_workspace
                    )
                    VALUES (
                        ?,
                        jsonb_build_object('allowed_chemicals', CAST(? AS jsonb)),
                        '{"offset":{"x":0,"y":0},"zoom_scale":1.0}'::jsonb,
                        '[]'::jsonb
                    )
                    """,
                    labId,
                    allowedChemicalsJson
            );
        }
    }

    private record InventorySeed(
            String itemCode,
            String name,
            String type,
            String state,
            String subCategory,
            String iconName,
            String iconColor,
            String iconFill
    ) {
    }

    private record LabSeed(
            String title,
            String description,
            String category,
            String difficulty,
            int maxScore,
            String allowedChemicalsJson
    ) {
    }
}
