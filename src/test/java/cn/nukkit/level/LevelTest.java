package cn.nukkit.level;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockPodzol;
import cn.nukkit.blockstate.BlockState;
import cn.nukkit.blockstate.BlockStateRegistry;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.generator.Flat;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.test.LogLevelAdjuster;
import co.aikar.timings.Timings;
import org.iq80.leveldb.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(PowerNukkitExtension.class)
class LevelTest {
    static final LogLevelAdjuster logLevelAdjuster = new LogLevelAdjuster(); 
    
    File levelFolder;
    
    Level level;
    String path;

    @Test
    void repairing() throws Exception {
        logLevelAdjuster.onlyNow(BlockStateRegistry.class, org.apache.logging.log4j.Level.OFF, ()->
                level.setBlockStateAt(2, 2, 2, BlockState.of(BlockID.PODZOL, 1))
        );
        Block block = level.getBlock(new Vector3(2, 2, 2));
        assertThat(block).isInstanceOf(BlockPodzol.class);
        assertEquals(BlockID.PODZOL, block.getId());
        assertEquals(0, block.getExactIntStorage());
        
        assertEquals(BlockState.of(BlockID.PODZOL), level.getBlockStateAt(2, 2, 2));
        
        assertTrue(level.unloadChunk(block.getChunkX(), block.getChunkZ()));

        assertEquals(BlockState.of(BlockID.PODZOL), level.getBlockStateAt(2, 2, 2));
    }

    @Test
    void customData() {
        assertTrue(level.setBlock(2, 2, 2, Block.get(BlockID.STONE), true, false));
        Block block = level.getBlock(2, 2, 2);
        assertEquals(BlockID.STONE, block.getId());
        Plugin plugin = mock(Plugin.class);
        when(plugin.getName()).thenReturn("APlugin");
        block.setCustomData(plugin, "MyData", 3);
        assertEquals(3, block.getCustomData(plugin).getInt("MyData"));

        Block airBlock = level.getBlock(3, 3, 3);
        airBlock.setCustomData(plugin, "TheAir", 4);
        assertEquals(4, airBlock.getCustomData(plugin).getInt("TheAir"));

        level.save(true);
        level.unload();

        Server server = Server.getInstance();
        level = new Level(server, "TestLevel", path, Anvil.class);
        level.gameRules = GameRules.getDefault();
        level.setAutoSave(true);

        server.getLevels().put(level.getId(), level);
        server.setDefaultLevel(level);

        block = level.getBlock(2, 2, 2);
        airBlock = level.getBlock(3, 3, 3);
        assertEquals(BlockID.STONE, block.getId());
        assertEquals(3, block.getCustomData(plugin).getInt("MyData"));
        assertEquals(4, airBlock.getCustomData(plugin).getInt("TheAir"));
    }

    @BeforeEach
    void setUp() throws IOException {
        Server server = Server.getInstance();
        levelFolder = new File(server.getDataPath(), "worlds/TestLevel");
        path = levelFolder.getAbsolutePath()+File.separator;
        Anvil.generate(path, "TestLevel", 0, Flat.class);
        Timings.init();
        level = new Level(server, "TestLevel", path, Anvil.class);
        level.gameRules = GameRules.getDefault();
        level.setAutoSave(true);
        
        server.getLevels().put(level.getId(), level);
        server.setDefaultLevel(level);
    }

    @AfterEach
    void tearDown() {
        try {
            level.unload();
        } finally {
            FileUtils.deleteRecursively(levelFolder);
        }
    }

    @AfterAll
    static void afterAll() {
        logLevelAdjuster.restoreLevels();
    }
}
