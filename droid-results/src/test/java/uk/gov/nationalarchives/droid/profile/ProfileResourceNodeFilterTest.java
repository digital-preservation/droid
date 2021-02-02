package uk.gov.nationalarchives.droid.profile;

import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.AbstractFilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ProfileResourceNodeFilterTest {



    @Test
    public void testNullFilterNotFiltered() {
        ProfileResourceNode node = new ProfileResourceNode();
        ProfileResourceNodeFilter nodeFilter = new ProfileResourceNodeFilter(null);
        assertFalse(nodeFilter.passesFilter(node));
    }

    @Test
    public void testEmptyConstructorNotFiltered() {
        ProfileResourceNode node = new ProfileResourceNode();
        ProfileResourceNodeFilter nodeFilter = new ProfileResourceNodeFilter();
        assertFalse(nodeFilter.passesFilter(node));
    }

    @Test
    public void testEmptyCriteriaNotFiltered() {
        ProfileResourceNode node = new ProfileResourceNode();
        ProfileResourceNodeFilter nodeFilter = new ProfileResourceNodeFilter(new FilterImpl());
        assertFalse(nodeFilter.passesFilter(node));
    }

    @Test
    public void testSizeFilterLessThan() {
        ProfileResourceNode sizeNode = createSizeNode(100000);
        ProfileResourceNodeFilter sizeFilter = createSizeFilter(CriterionOperator.LT,100);
        assertFalse(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(99);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(100);
        assertFalse(sizeFilter.passesFilter(sizeNode));
    }

    @Test
    public void testSizeFilterLessThanEqual() {
        ProfileResourceNode sizeNode = createSizeNode(100000);
        ProfileResourceNodeFilter sizeFilter = createSizeFilter(CriterionOperator.LTE,100);
        assertFalse(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(99);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(100);
        assertTrue(sizeFilter.passesFilter(sizeNode));
    }

    @Test
    public void testSizeFilterEqual() {
        ProfileResourceNode sizeNode = createSizeNode(100000);
        ProfileResourceNodeFilter sizeFilter = createSizeFilter(CriterionOperator.EQ,100);
        assertFalse(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(99);
        assertFalse(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(100);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(101);
        assertFalse(sizeFilter.passesFilter(sizeNode));
    }

    @Test
    public void testSizeFilterNotEqual() {
        ProfileResourceNode sizeNode = createSizeNode(100000);
        ProfileResourceNodeFilter sizeFilter = createSizeFilter(CriterionOperator.NE,100);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(99);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(100);
        assertFalse(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(101);
        assertTrue(sizeFilter.passesFilter(sizeNode));
    }

    @Test
    public void testSizeFilterGreaterThan() {
        ProfileResourceNode sizeNode = createSizeNode(100000);
        ProfileResourceNodeFilter sizeFilter = createSizeFilter(CriterionOperator.GT,100);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(100);
        assertFalse(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(101);
        assertTrue(sizeFilter.passesFilter(sizeNode));
    }

    @Test
    public void testSizeFilterGreaterThanEqual() {
        ProfileResourceNode sizeNode = createSizeNode(100000);
        ProfileResourceNodeFilter sizeFilter = createSizeFilter(CriterionOperator.GTE,100);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(100);
        assertTrue(sizeFilter.passesFilter(sizeNode));
        sizeNode = createSizeNode(99);
        assertFalse(sizeFilter.passesFilter(sizeNode));
    }

    @Test
    public void testFilenameLessThan() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.LT,"ABCDEF");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameLessThanEqual() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.LTE,"ABCDE");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABC");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameEqual() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.EQ,"ABCDE");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotEqual() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NE,"ABCDE");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameGreaterThan() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.GT,"ABCDEF");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameGreaterThanEqual() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.GTE,"ABCDE");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABC");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameStartsWith() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.STARTS_WITH,"AB");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABC");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotStartsWith() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NOT_STARTS_WITH,"AB");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABC");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameEndsWith() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.ENDS_WITH,"DE");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotEndsWith() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NOT_ENDS_WITH,"DE");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameContains() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.CONTAINS,"CD");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotContains() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NOT_CONTAINS,"CD");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testLastModifiedLessThan() {
        ProfileResourceNode lastModNode = createDateNode(new Date(10000));
        ProfileResourceNodeFilter lastModFilter = createDateFilter(CriterionOperator.LT, new Date(9999));
        assertFalse(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9998));
        assertTrue(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9999));
        assertFalse(lastModFilter.passesFilter(lastModNode));
    }

    @Test
    public void testLastModifiedLessThanEqual() {
        ProfileResourceNode lastModNode = createDateNode(new Date(10000));
        ProfileResourceNodeFilter lastModFilter = createDateFilter(CriterionOperator.LTE, new Date(9999));
        assertFalse(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9998));
        assertTrue(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9999));
        assertTrue(lastModFilter.passesFilter(lastModNode));
    }

    @Test
    public void testLastModifiedEqual() {
        ProfileResourceNode lastModNode = createDateNode(new Date(10000));
        ProfileResourceNodeFilter lastModFilter = createDateFilter(CriterionOperator.EQ, new Date(9999));
        assertFalse(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9999));
        assertTrue(lastModFilter.passesFilter(lastModNode));
    }

    @Test
    public void testLastModifiedNotEqual() {
        ProfileResourceNode lastModNode = createDateNode(new Date(10000));
        ProfileResourceNodeFilter lastModFilter = createDateFilter(CriterionOperator.NE, new Date(9999));
        assertTrue(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9999));
        assertFalse(lastModFilter.passesFilter(lastModNode));
    }


    @Test
    public void testLastModifiedGreaterThan() {
        ProfileResourceNode lastModNode = createDateNode(new Date(10000));
        ProfileResourceNodeFilter lastModFilter = createDateFilter(CriterionOperator.GT, new Date(9999));
        assertTrue(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9998));
        assertFalse(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9999));
        assertFalse(lastModFilter.passesFilter(lastModNode));
    }

    @Test
    public void testLastModifiedGreaterThanEqual() {
        ProfileResourceNode lastModNode = createDateNode(new Date(10000));
        ProfileResourceNodeFilter lastModFilter = createDateFilter(CriterionOperator.GTE, new Date(9999));
        assertTrue(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9998));
        assertFalse(lastModFilter.passesFilter(lastModNode));
        lastModNode = createDateNode(new Date(9999));
        assertTrue(lastModFilter.passesFilter(lastModNode));
    }

    @Test
    public void testResourceTypeAnyOf() {
        ProfileResourceNode resTypeNode = createResourceTypeNode(ResourceType.CONTAINER);
        ProfileResourceNodeFilter resTypeFilter = createResourceTypeFilter(CriterionOperator.ANY_OF, ResourceType.FILE, ResourceType.CONTAINER);
        assertTrue(resTypeFilter.passesFilter(resTypeNode));
        resTypeNode = createResourceTypeNode(ResourceType.FOLDER);
        assertFalse(resTypeFilter.passesFilter(resTypeNode));
    }


    @Test
    public void testResourceTypeNoneOf() {
        ProfileResourceNode resTypeNode = createResourceTypeNode(ResourceType.CONTAINER);
        ProfileResourceNodeFilter resTypeFilter = createResourceTypeFilter(CriterionOperator.NONE_OF, ResourceType.FILE, ResourceType.CONTAINER);
        assertFalse(resTypeFilter.passesFilter(resTypeNode));
        resTypeNode = createResourceTypeNode(ResourceType.FOLDER);
        assertTrue(resTypeFilter.passesFilter(resTypeNode));
    }

    @Test
    public void testIdMethodTypeAnyOf() {
        ProfileResourceNode resTypeNode = createIdMethodNode(IdentificationMethod.BINARY_SIGNATURE);
        ProfileResourceNodeFilter resTypeFilter = createIdMethodFilter(CriterionOperator.ANY_OF, IdentificationMethod.CONTAINER, IdentificationMethod.BINARY_SIGNATURE);
        assertTrue(resTypeFilter.passesFilter(resTypeNode));
        resTypeNode = createIdMethodNode(IdentificationMethod.EXTENSION);
        assertFalse(resTypeFilter.passesFilter(resTypeNode));
    }

    @Test
    public void testIdMethodTypeNoneOf() {
        ProfileResourceNode resTypeNode = createIdMethodNode(IdentificationMethod.BINARY_SIGNATURE);
        ProfileResourceNodeFilter resTypeFilter = createIdMethodFilter(CriterionOperator.NONE_OF, IdentificationMethod.CONTAINER, IdentificationMethod.BINARY_SIGNATURE);
        assertFalse(resTypeFilter.passesFilter(resTypeNode));
        resTypeNode = createIdMethodNode(IdentificationMethod.EXTENSION);
        assertTrue(resTypeFilter.passesFilter(resTypeNode));
    }

    @Test
    public void testJobStatusAnyOf() {
        ProfileResourceNode node = createNodeStatusNode(NodeStatus.DONE);
        ProfileResourceNodeFilter filter = createNodeStatusFilter(CriterionOperator.ANY_OF, NodeStatus.DONE, NodeStatus.ERROR);
        assertTrue(filter.passesFilter(node));
        node = createNodeStatusNode(NodeStatus.ACCESS_DENIED);
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testJobStatusNoneOf() {
        ProfileResourceNode node = createNodeStatusNode(NodeStatus.DONE);
        ProfileResourceNodeFilter filter = createNodeStatusFilter(CriterionOperator.NONE_OF, NodeStatus.DONE, NodeStatus.ERROR);
        assertFalse(filter.passesFilter(node));
        node = createNodeStatusNode(NodeStatus.ACCESS_DENIED);
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testSingleExtensionEqualTo() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.EQ, "bmp");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("BMP");
        assertTrue(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.EQ, "BMP");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionEqualTo() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.EQ, "bmp", "jpg");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("JPG");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("BMP");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("PDF");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.EQ, "bmp", "jpg", "pdf");
        assertTrue(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.EQ, "BMP", "jpg", "PDF");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("bMp");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testSingleExtensionNotEqualTo() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NE, "bmp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("BMP");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.NE, "BMP");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionNotEqualTo() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NE, "bmp", "jpg");
        System.out.println(filter.passesFilter(node));
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("JPG");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("BMP");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("PDF");
        assertTrue(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.NE, "bmp", "jpg", "pdf");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.NE, "BMP", "jpg", "PDF");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bMp");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testExtensionStartsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.STARTS_WITH, "b");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("Bmp");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testExtensionNotStartsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NOT_STARTS_WITH, "b");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("Bmp");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionStartsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.STARTS_WITH, "b", "jp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("Bmp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("JPg");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("pdf");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionNotStartsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NOT_STARTS_WITH, "b", "jp");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("Bmp");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("JPg");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("pdf");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testExtensionEndsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.ENDS_WITH, "p");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("Bmp");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testExtensionNotEndsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NOT_ENDS_WITH, "g");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("JPG");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("Bmp");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionEndsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.ENDS_WITH, "z", "pg");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("xyz");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("jpG");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("JPg");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("pdf");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionNotEndsWith() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NOT_ENDS_WITH, "b", "pg");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("xyz");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("jpG");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("JPg");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("pdf");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testExtensionContains() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.CONTAINS, "pg");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithpginthemiddle");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithPGinthemiddle");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("bmgp");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testExtensionNotContains() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NOT_CONTAINS, "pg");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithpginthemiddle");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithPGinthemiddle");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmgp");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionContains() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.CONTAINS, "pg", "mp");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithpginthemiddle");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithMpinthemiddle");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithmpinthemiddle");
        assertTrue(filter.passesFilter(node));
        node = createExtensionNode("bmgp");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testMultiExtensionNotContains() {
        ProfileResourceNode node = createExtensionNode("jpg");
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.NOT_CONTAINS, "pg", "mp");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithpginthemiddle");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithMpinthemiddle");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("abiggerwithmpinthemiddle");
        assertFalse(filter.passesFilter(node));
        node = createExtensionNode("bmgp");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testIdCountLessThan() {
        ProfileResourceNode node = createIdCountNode(1);
        ProfileResourceNodeFilter filter = createIdCountFilter(CriterionOperator.LT, 2);
        assertTrue(filter.passesFilter(node));
        node = createIdCountNode(2);
        assertFalse(filter.passesFilter(node));
        node = createIdCountNode(3);
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testIdCountLessThanEqual() {
        ProfileResourceNode node = createIdCountNode(1);
        ProfileResourceNodeFilter filter = createIdCountFilter(CriterionOperator.LTE, 2);
        assertTrue(filter.passesFilter(node));
        node = createIdCountNode(2);
        assertTrue(filter.passesFilter(node));
        node = createIdCountNode(3);
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testIdCountEqual() {
        ProfileResourceNode node = createIdCountNode(1);
        ProfileResourceNodeFilter filter = createIdCountFilter(CriterionOperator.EQ, 2);
        assertFalse(filter.passesFilter(node));
        node = createIdCountNode(2);
        assertTrue(filter.passesFilter(node));
        node = createIdCountNode(3);
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testIdCountNotEqual() {
        ProfileResourceNode node = createIdCountNode(1);
        ProfileResourceNodeFilter filter = createIdCountFilter(CriterionOperator.NE, 2);
        assertTrue(filter.passesFilter(node));
        node = createIdCountNode(2);
        assertFalse(filter.passesFilter(node));
        node = createIdCountNode(3);
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testIdCountGreaterThan() {
        ProfileResourceNode node = createIdCountNode(1);
        ProfileResourceNodeFilter filter = createIdCountFilter(CriterionOperator.GT, 2);
        assertFalse(filter.passesFilter(node));
        node = createIdCountNode(2);
        assertFalse(filter.passesFilter(node));
        node = createIdCountNode(3);
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testIdCountGreaterThanEqual() {
        ProfileResourceNode node = createIdCountNode(1);
        ProfileResourceNodeFilter filter = createIdCountFilter(CriterionOperator.GTE, 2);
        assertFalse(filter.passesFilter(node));
        node = createIdCountNode(2);
        assertTrue(filter.passesFilter(node));
        node = createIdCountNode(3);
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testExtensionMismatchEqual() {
        ProfileResourceNode node = createExtensionMismatchNode(true);
        ProfileResourceNodeFilter filter = createExtensionMismatchFilter(CriterionOperator.EQ, true);
        assertTrue(filter.passesFilter(node));
        node = createExtensionMismatchNode(false);
        assertFalse(filter.passesFilter(node));

        filter = createExtensionMismatchFilter(CriterionOperator.EQ, false);
        assertTrue(filter.passesFilter(node));
        node = createExtensionMismatchNode(true);
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testExtensionMismatchNotEqual() {
        ProfileResourceNode node = createExtensionMismatchNode(true);
        ProfileResourceNodeFilter filter = createExtensionMismatchFilter(CriterionOperator.NE, true);
        assertFalse(filter.passesFilter(node));
        node = createExtensionMismatchNode(false);
        assertTrue(filter.passesFilter(node));

        filter = createExtensionMismatchFilter(CriterionOperator.NE, false);
        assertFalse(filter.passesFilter(node));
        node = createExtensionMismatchNode(true);
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testPuidAnyOf() {
        ProfileResourceNode node = createFormatNode("fmt/2");
        ProfileResourceNodeFilter filter = createPuidNodeFilter(CriterionOperator.ANY_OF, "fmt/1", "fmt/2", "fmt/3");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("fmt/2", "fmt/4");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("fmt/4");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("fmt/4", "fmt/5", "fmt/0");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testPuidNoneOf() {
        ProfileResourceNode node = createFormatNode("fmt/2");
        ProfileResourceNodeFilter filter = createPuidNodeFilter(CriterionOperator.NONE_OF, "fmt/1", "fmt/2", "fmt/3");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("fmt/2", "fmt/4");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("fmt/4");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("fmt/4", "fmt/5", "fmt/0");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testFormatNameEquals() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.EQ, "Microsoft Word");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wor", "micrOsoFT worD");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testFormatNameNotEquals() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.NE, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wor", "micrOsoFT worD");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testFormatNameStartsWith() {
        fail("todo");
    }

    @Test
    public void testFormatNameNotStartsWith() {
        fail("todo");
    }

    @Test
    public void testFormatNameEndsWith() {
        fail("todo");

    }

    @Test
    public void testFormatNameNotEndsWith() {
        fail("todo");

    }

    @Test
    public void testFormatNameContains() {
        fail("todo");

    }

    @Test
    public void testFormatNameNotContains() {
        fail("todo");

    }

    //TODO: test mime type.

    //TODO: test what happens with filter if one of the ProfileResourceNode values is null.

    //TODO: test multi-criteria filtering

    //TODO: test widening and narrowing for multi-criteria filtering.

    /*
     * Helper methods to construct nodes with metadata and filters.
     */

    private ProfileResourceNodeFilter createFormatFilter(CriterionOperator operator, String value) {
        final FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.FILE_FORMAT, operator, value);
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNodeFilter createPuidNodeFilter(CriterionOperator operator, String... values) {

        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.PUID, operator, convertStringArray(values));
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createFormatNode(String... values) {
        ProfileResourceNode node = new ProfileResourceNode();
        for (int i = 0; i < values.length; i++) {
            node.addFormatIdentification(new Format(values[i], values[i], values[i], values[i]));
        }
        return node;
    }

    private ProfileResourceNode createExtensionMismatchNode(boolean value) {
        ProfileResourceNode node = new ProfileResourceNode();
        node.setExtensionMismatch(value);
        return node;
    }

    private ProfileResourceNodeFilter createExtensionMismatchFilter(CriterionOperator operator, boolean value) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.EXTENSION_MISMATCH, operator, value);
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNodeFilter createIdCountFilter(CriterionOperator operator, int count) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.IDENTIFICATION_COUNT, operator, count);
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createIdCountNode(int count) {
        ProfileResourceNode node = new ProfileResourceNode();
        for (int i = 0; i < count; i++) {
            node.addFormatIdentification(new Format());
        }
        return node;
    }

    private ProfileResourceNodeFilter createExtensionFilter(CriterionOperator operator, String... values) {
        final FilterCriterionTest criterion;
        if (values.length == 1) {
            criterion = new FilterCriterionTest(CriterionFieldEnum.FILE_EXTENSION, operator, values[0]);
        } else {
            criterion = new FilterCriterionTest(CriterionFieldEnum.FILE_EXTENSION, operator, convertStringArray(values));
        }
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createExtensionNode(String extension) {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setExtension(extension);
        node.setMetaData(metadata);
        return node;
    }

    private ProfileResourceNodeFilter createNodeStatusFilter(CriterionOperator operator, NodeStatus... statuses) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.JOB_STATUS, operator, convertObjectArray(statuses));
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createNodeStatusNode(NodeStatus status) {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setNodeStatus(status);
        node.setMetaData(metadata);
        return node;
    }

    private ProfileResourceNodeFilter createResourceTypeFilter(CriterionOperator operator, ResourceType... resourceTypes) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.RESOURCE_TYPE, operator, convertObjectArray(resourceTypes));
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createResourceTypeNode(ResourceType resourceType) {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setResourceType(resourceType);
        node.setMetaData(metadata);
        return node;
    }

    private ProfileResourceNodeFilter createIdMethodFilter(CriterionOperator operator, IdentificationMethod... methods) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.IDENTIFICATION_METHOD, operator, convertObjectArray(methods));
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createIdMethodNode(IdentificationMethod method) {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setIdentificationMethod(method);
        node.setMetaData(metadata);
        return node;
    }

    private ProfileResourceNodeFilter createDateFilter(CriterionOperator operator, Date date) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.LAST_MODIFIED_DATE, operator, date);
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createDateNode(Date date) {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setLastModifiedDate(date);
        node.setMetaData(metadata);
        return node;
    }

    private ProfileResourceNode createNameNode(String name) {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setName(name);
        node.setMetaData(metadata);
        return node;
    }

    private ProfileResourceNodeFilter createNameFilter(CriterionOperator operator, String name) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.FILE_NAME, operator, name);
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createSizeNode(long size) {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setSize(size);
        node.setMetaData(metadata);
        return node;
    }

    private ProfileResourceNodeFilter createSizeFilter(CriterionOperator operator, long size) {
        FilterCriterionTest criterion = new FilterCriterionTest(CriterionFieldEnum.FILE_SIZE, operator, size);
        FilterTest filter = new FilterTest(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private Object convertObjectArray(Enum<?>[] enums) {
        Object[] result = new Object[enums.length];
        for (int i = 0; i < enums.length; i++) {
            result[i] = enums[i];
        }
        return result;
    }

    private Object convertStringArray(String[] values) {
        Object[] result = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }

    /*
     *  Test implementations of Filter and FilterCriterion.
     */

    private static class FilterCriterionTest extends AbstractFilterCriterion<Object> {

        private Object value;

        public FilterCriterionTest(CriterionFieldEnum field, CriterionOperator operator, Object value) {
            super(field, operator);
            this.value = value;
        }

        @Override
        protected CriterionOperator[] operators() {
            return new CriterionOperator[0];
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    private static class FilterTest implements Filter {

        private List<FilterCriterion> criteria = new ArrayList<>();
        private boolean isNarrowed;

        public FilterTest(FilterCriterion criteria) {
            this.criteria.add(criteria);
        }

        public FilterTest(FilterCriterion criteria, boolean isNarrowed) {
            this.criteria.add(criteria);
            this.isNarrowed = isNarrowed;
        }

        public FilterTest(List<FilterCriterion> criteria) {
            this.criteria = criteria;
        }

        public FilterTest(List<FilterCriterion> criteria, boolean isNarrowed) {
            this.criteria = criteria;
            this.isNarrowed = isNarrowed;
        }


        @Override
        public List<FilterCriterion> getCriteria() {
            return criteria;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void setEnabled(boolean enabled) {
            //nothing to do.
        }

        @Override
        public boolean isNarrowed() {
            return isNarrowed;
        }

        @Override
        public boolean hasCriteria() {
            return criteria.size() > 0;
        }

        @Override
        public void setNarrowed(boolean isNarrowed) {
            this.isNarrowed = isNarrowed;
        }

        @Override
        public FilterCriterion getFilterCriterion(int index) {
            return criteria.get(index);
        }

        @Override
        public int getNumberOfFilterCriterion() {
            return criteria.size();
        }

        @Override
        public Filter clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }


}