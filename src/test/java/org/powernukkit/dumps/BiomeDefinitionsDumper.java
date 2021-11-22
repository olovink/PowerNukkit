package org.powernukkit.dumps;

import cn.nukkit.Server;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Log4j2
public class BiomeDefinitionsDumper {
    @SneakyThrows
    public static void main(String[] args) {
        //<editor-fold desc="Loading biome_definitions.dat" defaultstate="collapsed">
        CompoundTag tag;
        try (InputStream stream = Server.class.getClassLoader().getResourceAsStream("biome_definitions.dat")) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block state nbt");
            }

            try (BufferedInputStream bis = new BufferedInputStream(stream)) {
                tag = NBTIO.read(bis, ByteOrder.BIG_ENDIAN, true);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        //</editor-fold>

        Files.write(Paths.get("dumps/biome_definitions.dat.dump.txt"), tag.toString().getBytes(StandardCharsets.UTF_8));
    }
}
