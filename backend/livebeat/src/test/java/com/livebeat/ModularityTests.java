package com.livebeat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * [app] Spring Modulith 模組邊界驗證測試
 *
 * 負責：靜態分析所有模組的套件結構，確認無違反邊界規則（如跨模組直接呼叫 Repository）
 */
class ModularityTests {

    @Test
    void verifyModularity() {
        ApplicationModules.of(LivebeatApplication.class).verify();
    }
}
