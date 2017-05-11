/**
 * This package contains all classes that are needed by {@code RQuest} to 
 * describe a quest. Classes that are needed to actually run a quest should 
 * go in the {@code neon.narrative} package.
 * 
 * An {@code RQuest} may contain dialog. Dialog is arranged in a set of 
 * {@code Conversation}s. Each conversation has a root topic, which is shown 
 * when dialog is initiated with a creature, if the preconditions for that 
 * conversation are met. The root topic can have subtopics, which are shown 
 * when the root topic is chosen during conversation and the preconditions for
 * that subtopic are met. Each subtopic can have further subtopics. When a 
 * topic has no further subtopics, all valid root topics are shown again when
 * that topic has been answered.
 * 
 * @author mdriesen
 */
package neon.resources.quest;
