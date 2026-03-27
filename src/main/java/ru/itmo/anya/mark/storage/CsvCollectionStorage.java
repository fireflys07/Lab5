package ru.itmo.anya.mark.storage;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.nio.file.Path;
import java.util.List;

public class CsvCollectionStorage<T> extends AbstractFileStorage<T> {

    private final CsvMapper csvMapper;
    private final Class<T> clazz;

    public CsvCollectionStorage(Class<T> clazz) {
        this.csvMapper = new CsvMapper();
        this.clazz = clazz;
    }

    @Override
    public void save(List<T> items, Path path) throws Exception {
        super.ensureParentDirectoryExists(path);
        CsvSchema schema = csvMapper.schemaFor(clazz).withHeader();
        csvMapper.writer(schema).writeValue(path.toFile(), items);
    }

    @Override
    public List<T> load(Path path) throws Exception {
        CsvSchema schema = csvMapper.schemaFor(clazz).withHeader();
        return csvMapper.readerFor(clazz)
                .with(schema)
                .readValues(path.toFile())
                .readAll();
    }
}