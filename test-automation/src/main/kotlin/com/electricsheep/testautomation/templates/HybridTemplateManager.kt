package com.electricsheep.testautomation.templates

import org.slf4j.LoggerFactory
import java.io.File

/**
 * Hybrid template management combining reference templates with runtime discovery.
 * 
 * **Approach:**
 * - **Primary**: Reference templates (curated, reliable)
 * - **Secondary**: Runtime discovery (with strong validation)
 * 
 * **Reliability**: ⭐⭐⭐⭐⭐ (Very High)
 * - Reference templates provide stable base
 * - Runtime discovery adapts to actual app behavior
 * - Validation ensures quality
 * 
 * **Complexity**: ⭐⭐ (Medium)
 * - Requires managing two template sources
 * - Validation adds some complexity
 * - But provides best reliability/human-likeness balance
 * 
 * **Trade-off**: ✅ Worth it - Very high reliability, acceptable complexity
 */
class HybridTemplateManager(
    private val referenceTemplateDir: File,
    private val runtimeDiscovery: RuntimeTemplateDiscovery
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val referenceTemplates: Set<Template> = loadReferenceTemplates()
    
    /**
     * Template for pattern matching.
     * 
     * Shared between reference and runtime templates.
     */
    data class Template(
        val name: String,
        val image: File,
        val elementType: RuntimeTemplateDiscovery.ElementType
    ) {
        /**
         * Check if this template is similar to another.
         * 
         * Used to avoid duplicates between reference and runtime templates.
         */
        fun similarTo(other: Template): Boolean {
            // Same element type
            if (elementType != other.elementType) {
                return false
            }
            
            // Similar names (fuzzy matching)
            val nameSimilarity = calculateNameSimilarity(name, other.name)
            if (nameSimilarity > 0.8) {
                return true
            }
            
            // TODO: Visual similarity check (compare images)
            // if (visualSimilarity(image, other.image) > 0.9) return true
            
            return false
        }
        
        private fun calculateNameSimilarity(name1: String, name2: String): Double {
            // Simple similarity based on common words
            val words1 = name1.lowercase().split("_", "-")
            val words2 = name2.lowercase().split("_", "-")
            val commonWords = words1.intersect(words2).size
            val totalWords = maxOf(words1.size, words2.size)
            return if (totalWords > 0) commonWords.toDouble() / totalWords else 0.0
        }
    }
    
    /**
     * Get all available templates (reference + validated runtime).
     * 
     * Reference templates are always included (reliable).
     * Runtime templates are only included if validated.
     */
    fun getTemplates(): Set<Template> {
        val allTemplates = referenceTemplates.toMutableSet()
        
        // Add validated runtime templates
        val validatedRuntime = runtimeDiscovery.getValidatedTemplates()
        validatedRuntime.forEach { template ->
            // Check if similar to reference template (avoid duplicates)
            if (!referenceTemplates.any { it.similarTo(template) }) {
                allTemplates.add(template)
                logger.debug("Added validated runtime template: ${template.name}")
            } else {
                logger.debug("Skipped runtime template (similar to reference): ${template.name}")
            }
        }
        
        logger.info("Template count: ${referenceTemplates.size} reference + ${validatedRuntime.size} runtime = ${allTemplates.size} total")
        return allTemplates
    }
    
    /**
     * Discover templates from screenshot (runtime discovery).
     * 
     * @param screenshot The screenshot to analyze
     * @return List of newly validated templates
     */
    fun discoverTemplates(screenshot: File): List<Template> {
        return runtimeDiscovery.discoverTemplates(screenshot)
    }
    
    /**
     * Load reference templates from directory.
     * 
     * **Complexity**: ⭐ (Low - simple file loading)
     */
    private fun loadReferenceTemplates(): Set<Template> {
        val templates = mutableSetOf<Template>()
        
        if (!referenceTemplateDir.exists() || !referenceTemplateDir.isDirectory) {
            logger.warn("Reference template directory not found: ${referenceTemplateDir.absolutePath}")
            return templates
        }
        
        referenceTemplateDir.listFiles { _, name ->
            name.endsWith(".png", ignoreCase = true)
        }?.forEach { templateFile ->
            templates.add(
                Template(
                    name = templateFile.nameWithoutExtension,
                    image = templateFile,
                    elementType = inferElementType(templateFile.name)
                )
            )
        }
        
        logger.info("Loaded ${templates.size} reference templates from ${referenceTemplateDir.absolutePath}")
        return templates
    }
    
    /**
     * Infer element type from template name.
     */
    private fun inferElementType(name: String): RuntimeTemplateDiscovery.ElementType {
        return when {
            name.contains("icon", ignoreCase = true) -> RuntimeTemplateDiscovery.ElementType.ICON
            name.contains("button", ignoreCase = true) -> RuntimeTemplateDiscovery.ElementType.BUTTON
            name.contains("indicator", ignoreCase = true) || 
            name.contains("spinner", ignoreCase = true) || 
            name.contains("loading", ignoreCase = true) -> RuntimeTemplateDiscovery.ElementType.INDICATOR
            else -> RuntimeTemplateDiscovery.ElementType.OTHER
        }
    }
    
}

