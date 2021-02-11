/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.profile;

import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

public class ProfileResourceNodeFilterTest {

    @Test
    public void testNullFilterNotFiltered() {
        ProfileResourceNode node = new ProfileResourceNode();
        ProfileResourceNodeFilter nodeFilter = new ProfileResourceNodeFilter(null);
        assertTrue(nodeFilter.passesFilter(node));
    }

    @Test
    public void testEmptyConstructorNotFiltered() {
        ProfileResourceNode node = new ProfileResourceNode();
        ProfileResourceNodeFilter nodeFilter = new ProfileResourceNodeFilter();
        assertTrue(nodeFilter.passesFilter(node));
    }

    @Test
    public void testEmptyCriteriaNotFiltered() {
        ProfileResourceNode node = new ProfileResourceNode();
        ProfileResourceNodeFilter nodeFilter = new ProfileResourceNodeFilter(new FilterImpl());
        assertTrue(nodeFilter.passesFilter(node));
    }

    /*
      Filter out nodes which do not have a size set if we have size filter criteria.
     */
    @Test
    public void testNullSizeFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createSizeFilter(CriterionOperator.LT,100);
        assertFalse(filter.passesFilter(node));
        filter = createSizeFilter(CriterionOperator.LTE,100);
        assertFalse(filter.passesFilter(node));
        filter = createSizeFilter(CriterionOperator.EQ,100);
        assertFalse(filter.passesFilter(node));
        filter = createSizeFilter(CriterionOperator.NE,100);
        assertFalse(filter.passesFilter(node));
        filter = createSizeFilter(CriterionOperator.GT,100);
        assertFalse(filter.passesFilter(node));
        filter = createSizeFilter(CriterionOperator.GTE,100);
        assertFalse(filter.passesFilter(node));
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

    /*
      Filter out nodes which do not have a filename set if we have filename filter criteria.
     */
    @Test
    public void testNullFilenameFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createNameFilter(CriterionOperator.LT,"ABCDEF");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.LTE,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.EQ,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.NE,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.GT,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.GTE,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.STARTS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.NOT_STARTS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.ENDS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.NOT_ENDS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.CONTAINS,"ABCDE");
        assertFalse(filter.passesFilter(node));
        filter = createNameFilter(CriterionOperator.NOT_CONTAINS,"ABCDE");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testFilenameLessThan() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.LT,"ABCDEF");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdef");
        assertFalse(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdefg");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameLessThanEqual() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.LTE,"ABCDE");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABC");
        assertTrue(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdef");
        assertFalse(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdefg");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameEqual() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.EQ,"ABCDE");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdef");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotEqual() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NE,"ABCDE");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdef");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameGreaterThan() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.GT,"ABCDEF");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdefg");
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
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABC");
        assertTrue(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("AABC");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotStartsWith() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NOT_STARTS_WITH,"AB");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABC");
        assertFalse(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("BC");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameEndsWith() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.ENDS_WITH,"DE");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertTrue(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdef");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDEF");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotEndsWith() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NOT_ENDS_WITH,"DE");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertFalse(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("abcdef");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDEF");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameContains() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.CONTAINS,"CD");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertTrue(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertTrue(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("ABCEDFDCE");
        assertFalse(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testFilenameNotContains() {
        ProfileResourceNode nameNode = createNameNode("ABCDE");
        ProfileResourceNodeFilter nameFilter = createNameFilter(CriterionOperator.NOT_CONTAINS,"CD");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("abcde");
        assertFalse(nameFilter.passesFilter(nameNode));
        nameNode = createNameNode("ABCDDFFFDDE");
        assertFalse(nameFilter.passesFilter(nameNode));

        nameNode = createNameNode("ABCEDFDCE");
        assertTrue(nameFilter.passesFilter(nameNode));
    }

    @Test
    public void testLastModifiedNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createDateFilter(CriterionOperator.LT, new Date(9999));
        assertFalse(filter.passesFilter(node));
        filter = createDateFilter(CriterionOperator.LTE, new Date(9999));
        assertFalse(filter.passesFilter(node));
        filter = createDateFilter(CriterionOperator.EQ, new Date(9999));
        assertFalse(filter.passesFilter(node));
        filter = createDateFilter(CriterionOperator.NE, new Date(9999));
        assertFalse(filter.passesFilter(node));
        filter = createDateFilter(CriterionOperator.GT, new Date(9999));
        assertFalse(filter.passesFilter(node));
        filter = createDateFilter(CriterionOperator.GTE, new Date(9999));
        assertFalse(filter.passesFilter(node));
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
    public void testResourceTypeNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createResourceTypeFilter(CriterionOperator.ANY_OF, ResourceType.FILE, ResourceType.CONTAINER);
        assertFalse(filter.passesFilter(node));
        filter = createResourceTypeFilter(CriterionOperator.NONE_OF, ResourceType.FILE, ResourceType.CONTAINER);
        assertFalse(filter.passesFilter(node));
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
    public void testIdMethodNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createIdMethodFilter(CriterionOperator.ANY_OF, IdentificationMethod.CONTAINER, IdentificationMethod.BINARY_SIGNATURE);
        assertFalse(filter.passesFilter(node));
        filter = createIdMethodFilter(CriterionOperator.NONE_OF, IdentificationMethod.CONTAINER, IdentificationMethod.BINARY_SIGNATURE);
        assertFalse(filter.passesFilter(node));
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
    public void testJobStatusNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createNodeStatusFilter(CriterionOperator.ANY_OF, NodeStatus.DONE, NodeStatus.ERROR);
        assertFalse(filter.passesFilter(node));
        filter = createNodeStatusFilter(CriterionOperator.NONE_OF, NodeStatus.DONE, NodeStatus.ERROR);
        assertFalse(filter.passesFilter(node));
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
    public void testExtensionNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createExtensionFilter(CriterionOperator.EQ, "bmp");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.NE, "bmp");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.STARTS_WITH, "bmp");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.NOT_STARTS_WITH, "bmp");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.ENDS_WITH, "bmp");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.NOT_ENDS_WITH, "bmp");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.CONTAINS, "bmp");
        assertFalse(filter.passesFilter(node));
        filter = createExtensionFilter(CriterionOperator.NOT_CONTAINS, "bmp");
        assertFalse(filter.passesFilter(node));
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

    // No need to test null id count - this is a calculated value on the number of identifications - it can never be null.

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
    public void testExtensionMismatchNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createExtensionMismatchFilter(CriterionOperator.EQ, true);
        assertFalse(filter.passesFilter(node));
        filter = createExtensionMismatchFilter(CriterionOperator.NE, true);
        assertFalse(filter.passesFilter(node));
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
    public void testPuidNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createPuidNodeFilter(CriterionOperator.ANY_OF, "fmt/1", "fmt/2", "fmt/3");
        assertFalse(filter.passesFilter(node));

        /**
         * In the case of puids, if we ask for none of them and we have a null value, then it passes (as null isn't one of them).
         */
        filter = createPuidNodeFilter(CriterionOperator.NONE_OF, "fmt/1", "fmt/2", "fmt/3");
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
    public void testFormatNameNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.EQ, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        filter = createFormatFilter(CriterionOperator.NE, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        filter = createFormatFilter(CriterionOperator.STARTS_WITH, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        filter = createFormatFilter(CriterionOperator.NOT_STARTS_WITH, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        filter = createFormatFilter(CriterionOperator.ENDS_WITH, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        filter = createFormatFilter(CriterionOperator.NOT_ENDS_WITH, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        filter = createFormatFilter(CriterionOperator.CONTAINS, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
        filter = createFormatFilter(CriterionOperator.NOT_CONTAINS, "Microsoft Word");
        assertFalse(filter.passesFilter(node));
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

        node = createFormatNode("Microsoft Wor", "micrOsoFT excel");
        assertFalse(filter.passesFilter(node));
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

        node = createFormatNode("Microsoft Wor", "micrOsoFT excel");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testFormatNameStartsWith() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.STARTS_WITH, "Microsoft");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wor", "micrOsoFT worD");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worD");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testFormatNameNotStartsWith() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.NOT_STARTS_WITH, "Microsoft");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wor", "micrOsoFT worD");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worD");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testFormatNameEndsWith() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.ENDS_WITH, "t WoRd");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wor", "micrOsoFT worD");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worD");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worDx");
        assertFalse(filter.passesFilter(node));

    }

    @Test
    public void testFormatNameNotEndsWith() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.NOT_ENDS_WITH, "t WoRd");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wor", "micrOsoFT worD");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worD");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worDx");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testFormatNameContains() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.CONTAINS, "t WoR");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wr", "micrOsoFT  worD");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worD");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("icrosoft  Wor", "micrsoFT w orDx");
        assertFalse(filter.passesFilter(node));

    }

    @Test
    public void testFormatNameNotContains() {
        ProfileResourceNode node = createFormatNode("microsoft word");
        ProfileResourceNodeFilter filter = createFormatFilter(CriterionOperator.NOT_CONTAINS, "t WoR");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("Microsoft Wor");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("Microsoft Wr", "micrOsoFT  worD");
        assertTrue(filter.passesFilter(node));

        node = createFormatNode("icrosoft Wor", "micrsoFT worD");
        assertFalse(filter.passesFilter(node));

        node = createFormatNode("icrosoft  Wor", "micrsoFT wo rDx");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testMimeTypeNullFiltered() {
        ProfileResourceNode node = createNullNode();
        ProfileResourceNodeFilter filter = createMimeTypeFilter(CriterionOperator.ANY_OF, "text/plain");
        assertFalse(filter.passesFilter(node));

        /**
         * In the case of mime types, if we have a null value and we're looking at none of them, it passes as null isn't one of them.
         */
        filter = createMimeTypeFilter(CriterionOperator.NONE_OF, "text/plain");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testMimeTypeAnyOf() {
        // Test one to one passes:
        ProfileResourceNode node = createFormatNode("text/plain");
        ProfileResourceNodeFilter filter = createMimeTypeFilter(CriterionOperator.ANY_OF, "text/plain");
        assertTrue(filter.passesFilter(node));

        // Mime type is case sensitive
        node = createFormatNode("text/PLAIN");
        assertFalse(filter.passesFilter(node));

        // Test one to one failure:
        node = createFormatNode("text/plai");
        assertFalse(filter.passesFilter(node));

        // Test many to one passes:
        node = createFormatNode("something/else", "one/two", "text/plain", "another/thing");
        assertTrue(filter.passesFilter(node));

        // Test many to one failure:
        node = createFormatNode("something/else", "one/two", "tet/plain", "another/thing");
        assertFalse(filter.passesFilter(node));

        // Test one to many passes:
        filter = createMimeTypeFilter(CriterionOperator.ANY_OF, "text/plain", "one/two", "another/thing");
        node = createFormatNode("one/two");
        assertTrue(filter.passesFilter(node));

        // Test one to many failure:
        node = createFormatNode("one/to");
        assertFalse(filter.passesFilter(node));

        // Test many to many passes:
        node = createFormatNode("text/plain", "one/two", "another/thing");
        assertTrue(filter.passesFilter(node));
        node = createFormatNode("extra/stuff", "three/four", "more/ids", "one/two");
        assertTrue(filter.passesFilter(node));

        // Test many to many failure:
        node = createFormatNode("thing/explainer", "howto/invent", "anything/goes", "whatever/youlike");
        assertFalse(filter.passesFilter(node));
    }

    @Test
    public void testMimeTypeNoneOf() {
        // Test one to one failure:
        ProfileResourceNode node = createFormatNode("text/plain");
        ProfileResourceNodeFilter filter = createMimeTypeFilter(CriterionOperator.NONE_OF, "text/plain");
        assertFalse(filter.passesFilter(node));

        // Test one to one pass:
        node = createFormatNode("text/plai");
        assertTrue(filter.passesFilter(node));

        // Test many to one failure:
        node = createFormatNode("something/else", "one/two", "text/plain", "another/thing");
        assertFalse(filter.passesFilter(node));

        // Test many to one pass:
        node = createFormatNode("something/else", "one/two", "tet/plain", "another/thing");
        assertTrue(filter.passesFilter(node));

        // Test one to many failure:
        filter = createMimeTypeFilter(CriterionOperator.NONE_OF, "text/plain", "one/two", "another/thing");
        node = createFormatNode("one/two");
        assertFalse(filter.passesFilter(node));

        // Test one to many pass:
        node = createFormatNode("one/to");
        assertTrue(filter.passesFilter(node));

        // Test many to many failure:
        node = createFormatNode("text/plain", "one/two", "another/thing");
        assertFalse(filter.passesFilter(node));
        node = createFormatNode("extra/stuff", "three/four", "more/ids", "one/two");
        assertFalse(filter.passesFilter(node));

        // Test many to many pass:
        node = createFormatNode("thing/explainer", "howto/invent", "anything/goes", "whatever/youlike");
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testWidenFiltering() {
        ProfileResourceNode node = createFullNode();

        FilterCriterion extBmp = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "bmp");
        FilterCriterion extJpg = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "jpg");

        FilterCriterion resTypeFile = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, CriterionOperator.ANY_OF, new Object[] {ResourceType.FILE});
        FilterCriterion resTypeFolder = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, CriterionOperator.ANY_OF, new Object[] {ResourceType.FOLDER});
        FilterCriterion resTypeFileFolder = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, CriterionOperator.ANY_OF,
                new Object[] {ResourceType.FOLDER, ResourceType.FILE});

        ProfileResourceNodeFilter filter = createMultiFilter(false, extBmp, resTypeFile);
        assertTrue(filter.passesFilter(node));

        filter = createMultiFilter(false, extBmp, resTypeFolder);
        assertTrue(filter.passesFilter(node));

        filter = createMultiFilter(false, extJpg, resTypeFile);
        assertTrue(filter.passesFilter(node));

        filter = createMultiFilter(false, extJpg, resTypeFolder);
        assertFalse(filter.passesFilter(node));

        filter = createMultiFilter(false, extJpg, resTypeFileFolder);
        assertTrue(filter.passesFilter(node));
    }

    @Test
    public void testNarrowFiltering() {
        ProfileResourceNode node = createFullNode();

        FilterCriterion extBmp = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "bmp");
        FilterCriterion extJpg = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "jpg");

        FilterCriterion resTypeFile = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, CriterionOperator.ANY_OF, new Object[] {ResourceType.FILE});
        FilterCriterion resTypeFolder = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, CriterionOperator.ANY_OF, new Object[] {ResourceType.FOLDER});
        FilterCriterion resTypeFileFolder = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, CriterionOperator.ANY_OF,
                new Object[] {ResourceType.FOLDER, ResourceType.FILE});

        ProfileResourceNodeFilter filter = createMultiFilter(true, extBmp, resTypeFile);
        assertTrue(filter.passesFilter(node));

        filter = createMultiFilter(true, extBmp, resTypeFolder);
        assertFalse(filter.passesFilter(node));

        filter = createMultiFilter(true, extJpg, resTypeFile);
        assertFalse(filter.passesFilter(node));

        filter = createMultiFilter(true, extJpg, resTypeFolder);
        assertFalse(filter.passesFilter(node));

        filter = createMultiFilter(true, extJpg, resTypeFileFolder);
        assertFalse(filter.passesFilter(node));

        filter = createMultiFilter(true, extBmp, resTypeFileFolder);
        assertTrue(filter.passesFilter(node));
    }

    /*
     * Helper methods to construct nodes with metadata and filters.
     */

    private ProfileResourceNodeFilter createMultiFilter(boolean narrowed, FilterCriterion... criteria) {
        Filter filter = new BasicFilter(Arrays.asList(criteria), narrowed);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNode createFullNode() {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        metadata.setExtension("bmp");
        metadata.setNodeStatus(NodeStatus.DONE);
        metadata.setIdentificationMethod(IdentificationMethod.BINARY_SIGNATURE);
        metadata.setResourceType(ResourceType.FILE);
        metadata.setName("funny.bmp");
        metadata.setLastModifiedDate(new Date(0));
        metadata.setSize(1000L);
        node.setMetaData(metadata);
        for (int i = 0; i < 3; i++) {
            node.addFormatIdentification(new Format("bitmap", "graphics/thing", "bitmap thing", "1.0"));
        }
        node.setId(1L);
        node.setParentId(0L);
        return node;
    }

    private ProfileResourceNode createNullNode() {
        ProfileResourceNode node = new ProfileResourceNode();
        NodeMetaData metadata = new NodeMetaData();
        node.setMetaData(metadata);
        node.setExtensionMismatch(null); // defaults to false in the profile resource node currently.
        node.addFormatIdentification(new Format(null, null, null, null));
        node.addFormatIdentification(new Format(null, null, null, null));
        return node;
    }

    private ProfileResourceNodeFilter createMimeTypeFilter(CriterionOperator operator, String... values) {
        final BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.MIME_TYPE, operator, convertStringArray(values));
        Filter filter = new BasicFilter(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNodeFilter createFormatFilter(CriterionOperator operator, String value) {
        final BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_FORMAT, operator, value);
        Filter filter = new BasicFilter(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNodeFilter createPuidNodeFilter(CriterionOperator operator, String... values) {

        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.PUID, operator, convertStringArray(values));
        Filter filter = new BasicFilter(criterion);
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
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.EXTENSION_MISMATCH, operator, value);
        Filter filter = new BasicFilter(criterion);
        return new ProfileResourceNodeFilter(filter);
    }

    private ProfileResourceNodeFilter createIdCountFilter(CriterionOperator operator, int count) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.IDENTIFICATION_COUNT, operator, count);
        Filter filter = new BasicFilter(criterion);
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
        final BasicFilterCriterion criterion;
        if (values.length == 1) {
            criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, operator, values[0]);
        } else {
            criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, operator, convertStringArray(values));
        }
        Filter filter = new BasicFilter(criterion);
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
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.JOB_STATUS, operator, convertObjectArray(statuses));
        Filter filter = new BasicFilter(criterion);
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
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, operator, convertObjectArray(resourceTypes));
        Filter filter = new BasicFilter(criterion);
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
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.IDENTIFICATION_METHOD, operator, convertObjectArray(methods));
        Filter filter = new BasicFilter(criterion);
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
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.LAST_MODIFIED_DATE, operator, date);
        Filter filter = new BasicFilter(criterion);
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
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_NAME, operator, name);
        Filter filter = new BasicFilter(criterion);
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
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, operator, size);
        Filter filter = new BasicFilter(criterion);
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


}