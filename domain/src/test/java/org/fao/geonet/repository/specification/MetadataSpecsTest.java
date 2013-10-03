package org.fao.geonet.repository.specification;

import static junit.framework.Assert.*;
import static org.fao.geonet.repository.specification.MetadataSpecs.*;
import static org.fao.geonet.repository.MetadataRepositoryTest.*;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for MetadataSpecs.
 *
 * User: Jesse
 * Date: 9/4/13
 * Time: 10:08 PM
 */
@Transactional
public class MetadataSpecsTest extends AbstractSpringDataTest {
    @Autowired
    private MetadataRepository _repository;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testHasMetadataId() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        assertFindsCorrectMd(md1, hasMetadataId(md1.getId()), true);
    }

    @Test
    public void testHasMetadataType() throws Exception {
        final Metadata metadata = newMetadata(_inc);
        metadata.getDataInfo().setType(MetadataType.METADATA);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        Metadata md2 = _repository.save(metadata2);

        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getDataInfo().setType(MetadataType.TEMPLATE);
        Metadata md3 = _repository.save(metadata3);

        assertEquals(1, _repository.findAll(hasType(MetadataType.METADATA)).size());
        assertEquals(1, _repository.findAll(hasType(MetadataType.SUB_TEMPLATE)).size());
        assertEquals(1, _repository.findAll(hasType(MetadataType.TEMPLATE)).size());

        assertEquals(md1.getId(), _repository.findOne(hasType(MetadataType.METADATA)).getId());
        assertEquals(md2.getId(), _repository.findOne(hasType(MetadataType.SUB_TEMPLATE)).getId());
        assertEquals(md3.getId(), _repository.findOne(hasType(MetadataType.TEMPLATE)).getId());
    }

    @Test
    public void testIsOwnedByOneOfFollowingGroups() throws Exception {


        final Metadata metadata = newMetadata(_inc);
        metadata.getSourceInfo().setGroupOwner(1);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getSourceInfo().setGroupOwner(2);
        _repository.save(metadata2);


        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getSourceInfo().setGroupOwner(3);
        Metadata md3 = _repository.save(metadata3);

        List<Metadata> found = _repository.findAll(isOwnedByOneOfFollowingGroups(Arrays.asList(1)));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());

        found = _repository.findAll(isOwnedByOneOfFollowingGroups(Arrays.asList(1,3)), new Sort(Metadata_.id.getName()));
        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
        assertEquals(md3.getId(), found.get(1).getId());
    }

    @Test
    public void testHasMetadataUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasMetadataUuid(md1.getUuid());

        assertFindsCorrectMd(md1, spec, true);
    }


    @Test
    public void testHasHarvesterUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasHarvesterUuid(md1.getHarvestInfo().getUuid());
        assertFindsCorrectMd(md1,spec, true);
    }

    @Test
    public void testIsHarvested() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1.getHarvestInfo().setHarvested(false);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2.getHarvestInfo().setHarvested(true);
        md2 = _repository.save(md2);

        assertFindsCorrectMd(md1, isHarvested(false), false);
        assertFindsCorrectMd(md2, isHarvested(true), false);
    }

    @Test
    public void testHasMetadataIdIn() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2 = _repository.save(md2);

        List<Metadata> all = _repository.findAll(hasMetadataIdIn(Arrays.asList(md1.getId())));
        assertEquals(1, all.size());
        assertEquals(md1.getId(), all.get(0).getId());

        all = _repository.findAll(hasMetadataIdIn(Arrays.asList(md1.getId(), md2.getId())));
        assertEquals(2, all.size());

        all = _repository.findAll(hasMetadataIdIn(Collections.<Integer>emptyList()));
        assertTrue(all.isEmpty());


    }

    @Test
    public void testIsTemplate() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1.getDataInfo().setType(MetadataType.METADATA);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        md2 = _repository.save(md2);

        assertFindsCorrectMd(md1, isType(MetadataType.METADATA), false);
        assertFindsCorrectMd(md2, isType(MetadataType.SUB_TEMPLATE), false);
    }

    @Test
    public void testHasSource() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasSource(md1.getSourceInfo().getSourceId());
        assertFindsCorrectMd(md1,spec, true);
    }

    private void assertFindsCorrectMd(Metadata md1, Specification<Metadata> spec, boolean addNewMetadata) {
        if (addNewMetadata) {
            _repository.save(newMetadata(_inc));
        }
        List<Integer> found = _repository.findAllIdsBy(spec);
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).intValue());
    }
}