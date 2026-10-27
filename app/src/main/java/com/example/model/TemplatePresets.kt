package com.example.model

import java.util.UUID

data class EmailTemplate(
  val id: String,
  val title: String,
  val description: String,
  val iconEmoji: String,
  val subject: String,
  val blocks: List<EmailBlock>
)

object TemplatePresets {
  val allTemplates: List<EmailTemplate> = listOf(
    EmailTemplate(
      id = "welcome",
      title = "Welcome & Onboarding",
      description = "Friendly introduction with celebration GIF and quick start button",
      iconEmoji = "🎉",
      subject = "Welcome aboard! Let's get started 🚀",
      blocks = listOf(
        EmailBlock.Header(
          id = UUID.randomUUID().toString(),
          text = "Welcome to Our Community! ✨",
          subtitle = "We are thrilled to have you here",
          align = "center",
          color = "#1E293B"
        ),
        EmailBlock.GifImage(
          id = UUID.randomUUID().toString(),
          imageUrl = "https://media.giphy.com/media/26AHONQ79FdWZhAI0/giphy.gif",
          altText = "Welcome celebration",
          caption = "Ready to build something amazing?",
          align = "center"
        ),
        EmailBlock.Text(
          id = UUID.randomUUID().toString(),
          content = "Hi there! Your account is now fully active. We built this platform to make email design and dispatch effortless, beautiful, and powerful. Explore your dashboard below.",
          align = "left"
        ),
        EmailBlock.EmbedCard(
          id = UUID.randomUUID().toString(),
          badge = "QUICK START",
          badgeColor = "#4F46E5",
          title = "Complete Your Profile (2 min)",
          body = "Connect your services, customize your signature, and set up your personal branding.",
          linkText = "Go to Profile Setup →",
          linkUrl = "https://google.com"
        ),
        EmailBlock.Button(
          id = UUID.randomUUID().toString(),
          label = "Launch Dashboard 🚀",
          url = "https://google.com",
          backgroundColor = "#4F46E5",
          cornerRadiusDp = 8,
          align = "center"
        ),
        EmailBlock.Divider(id = UUID.randomUUID().toString()),
        EmailBlock.Footer(
          id = UUID.randomUUID().toString(),
          signOff = "Warmly,\nThe MailCraft Team"
        )
      )
    ),
    EmailTemplate(
      id = "product_announcement",
      title = "Product Launch",
      description = "Feature highlight with bold CTA and visual badge",
      iconEmoji = "🚀",
      subject = "Introducing MailCraft 2.0: Rich Email Studio ⚡",
      blocks = listOf(
        EmailBlock.Header(
          id = UUID.randomUUID().toString(),
          text = "Say Hello to MailCraft 2.0 ⚡",
          subtitle = "Custom buttons, embedded cards, and interactive GIFs",
          align = "center",
          color = "#0F172A"
        ),
        EmailBlock.EmbedCard(
          id = UUID.randomUUID().toString(),
          badge = "NEW RELEASE",
          badgeColor = "#10B981",
          title = "What's New in This Update",
          body = "• Bulletproof email buttons that work on every mail client\n• Dynamic GIF embeddings\n• Responsive layouts optimized for mobile and desktop\n• Native Gmail SMTP integration",
          linkText = "Read the release notes →",
          linkUrl = "https://google.com"
        ),
        EmailBlock.GifImage(
          id = UUID.randomUUID().toString(),
          imageUrl = "https://media.giphy.com/media/3o7TKSjRrfIPjeiVyM/giphy.gif",
          altText = "Celebration",
          caption = "Engineered with precision for creators and developers",
          align = "center"
        ),
        EmailBlock.Button(
          id = UUID.randomUUID().toString(),
          label = "Try Features Now →",
          url = "https://google.com",
          backgroundColor = "#0F172A",
          textColor = "#FFFFFF",
          cornerRadiusDp = 24,
          align = "center"
        ),
        EmailBlock.SocialLinks(id = UUID.randomUUID().toString()),
        EmailBlock.Footer(
          id = UUID.randomUUID().toString(),
          signOff = "Cheers,\nProduct & Engineering"
        )
      )
    ),
    EmailTemplate(
      id = "event_rsvp",
      title = "Meeting & Event RSVP",
      description = "Event details embed box with calendar links and response buttons",
      iconEmoji = "📅",
      subject = "Invitation: Q4 Strategy & Product Sync",
      blocks = listOf(
        EmailBlock.Header(
          id = UUID.randomUUID().toString(),
          text = "You're Invited: Product Sync 📅",
          subtitle = "Join us live on Google Meet",
          align = "center"
        ),
        EmailBlock.EmbedCard(
          id = UUID.randomUUID().toString(),
          badge = "EVENT DETAILS",
          badgeColor = "#E11D48",
          title = "Thursday, Oct 24 • 10:00 AM PST",
          body = "Agenda: Review quarterly roadmap, showcase email sender architecture, and live Q&A session.",
          linkText = "Google Meet Link 🎥",
          linkUrl = "https://meet.google.com"
        ),
        EmailBlock.Text(
          id = UUID.randomUUID().toString(),
          content = "Please confirm your availability so we can prepare resources and shared materials ahead of time.",
          align = "center"
        ),
        EmailBlock.Button(
          id = UUID.randomUUID().toString(),
          label = "Accept & Add to Calendar",
          url = "https://calendar.google.com",
          backgroundColor = "#2563EB",
          align = "center"
        ),
        EmailBlock.Divider(id = UUID.randomUUID().toString()),
        EmailBlock.Footer(id = UUID.randomUUID().toString())
      )
    ),
    EmailTemplate(
      id = "appreciation",
      title = "Thank You & Celebration",
      description = "Heartwarming note with celebration GIF and custom button",
      iconEmoji = "💖",
      subject = "A huge thank you from all of us! 🌟",
      blocks = listOf(
        EmailBlock.Header(
          id = UUID.randomUUID().toString(),
          text = "Thank You So Much! 🌟",
          subtitle = "We couldn't have done it without you",
          align = "center",
          color = "#4338CA"
        ),
        EmailBlock.GifImage(
          id = UUID.randomUUID().toString(),
          imageUrl = "https://media.giphy.com/media/osAcIGJnyeBq30qDCM/giphy.gif",
          altText = "Thank you animation",
          caption = "You are truly awesome!",
          align = "center"
        ),
        EmailBlock.Text(
          id = UUID.randomUUID().toString(),
          content = "Thank you for collaborating with us on this journey. Your feedback and support mean the world to our team. As a token of our appreciation, please claim your exclusive perk below.",
          align = "center"
        ),
        EmailBlock.Button(
          id = UUID.randomUUID().toString(),
          label = "Claim Your Special Gift 🎁",
          url = "https://google.com",
          backgroundColor = "#D97706",
          align = "center"
        ),
        EmailBlock.Footer(
          id = UUID.randomUUID().toString(),
          signOff = "With sincere gratitude,\nThe Team"
        )
      )
    ),
    EmailTemplate(
      id = "newsletter",
      title = "Weekly Digest & Newsletter",
      description = "Curated article embeds with read more buttons and divider sections",
      iconEmoji = "📰",
      subject = "This Week in Tech & Design (Issue #42) 🗞️",
      blocks = listOf(
        EmailBlock.Header(
          id = UUID.randomUUID().toString(),
          text = "The Weekly Dispatch 🗞️",
          subtitle = "Curated thoughts, tools, and inspirations",
          align = "center"
        ),
        EmailBlock.EmbedCard(
          id = UUID.randomUUID().toString(),
          badge = "FEATURED READ",
          badgeColor = "#7C3AED",
          title = "Mastering Modern Email HTML in 2026",
          body = "How responsive table designs, mso fallback buttons, and lightweight assets guarantee 100% rendering fidelity across every mobile and desktop email client.",
          linkText = "Read 5-min article →",
          linkUrl = "https://google.com"
        ),
        EmailBlock.Button(
          id = UUID.randomUUID().toString(),
          label = "Read Full Issue 📖",
          url = "https://google.com",
          backgroundColor = "#7C3AED",
          align = "center"
        ),
        EmailBlock.Divider(id = UUID.randomUUID().toString()),
        EmailBlock.SocialLinks(id = UUID.randomUUID().toString()),
        EmailBlock.Footer(id = UUID.randomUUID().toString())
      )
    )
  )
}
