# 🎯 AI Job Applier

**Intelligent job application system powered by Google Gemini AI**

An end-to-end solution for job seekers that analyzes job postings, calculates match scores, generates personalized cover letters, and tracks applications—all with AI assistance.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Gemini AI](https://img.shields.io/badge/Gemini-AI-purple)

---

## ✨ Features

### 🚀 Instant Job Analysis
- **Paste any job URL** → Get AI-powered match analysis in 3-5 seconds
- Works with LinkedIn, Naukri, company career pages, and more
- No web scraping issues—uses intelligent HTML parsing

### 🤖 AI-Powered Matching
- **Hybrid algorithm**: 70% rule-based + 30% AI semantic analysis
- Analyzes skills (40%), experience (20%), education (10%), + AI insights (30%)
- Match levels: Excellent (80%+), Strong (70-79%), Good (60-69%), Weak (40-59%), Poor (<40%)

### ✍️ Personalized Cover Letters
- Auto-generated using Google Gemini API
- Tailored to specific job requirements
- Created for matches ≥ 60%

### 📊 Application Tracker Dashboard
- Beautiful web UI with real-time statistics
- Color-coded match badges (Excellent, Good, Weak, Poor)
- Status management (Pending, Applied, Rejected)
- Cover letter viewer with modal popup
- Smart filtering and live refresh

### 📱 Telegram Bot Integration
- Mobile-friendly job analysis on the go
- Commands: `/start`, `/help`, `/daily`, `/weekly`
- Paste URLs directly in chat for instant analysis
- Formatted responses with emojis

### 📄 Resume Parsing
- Upload PDF resumes
- Auto-extract skills, experience, education
- Store in database for instant matching

---





### Application Tracker Dashboard
```
┌─────────────────────────────────────────────────────┐
│  Total: 10  │ Applied: 3  │ Pending: 5  │ Avg: 72% │
└─────────────────────────────────────────────────────┘

┌─ Senior Java Developer ─────────────────────┐
│ Microsoft | Hyderabad                  [74%]│
│ Status: PENDING      🔗 View  📄 Letter     │
└─────────────────────────────────────────────┘
```

### Telegram Bot
```
User: https://careers.microsoft.com/job/123
Bot:  🎯 74% - Strong Match
      ✅ Matching: Java, Spring Boot, MySQL
      ❌ Missing: AWS
      📄 Cover letter generated!
```

---



## 🔌 API Endpoints

### Job Analysis
- `GET /api/jobs/analyze?url={jobUrl}` - Analyze job from URL
- `GET /api/jobs/matched` - Get matched jobs (≥60%)
- `GET /api/jobs/stats` - Get statistics

### Application Tracker
- `GET /api/tracker/applications` - All applications
- `GET /api/tracker/stats` - Dashboard stats
- `GET /api/tracker/cover-letter/{id}` - Get cover letter
- `PUT /api/tracker/status/{id}` - Update status

### Resume
- `POST /api/resume/upload` - Upload resume
- `GET /api/resume/active` - Get active resume

### Manual Entry
- `POST /api/jobs/manual/quick-add` - Add single job
- `POST /api/jobs/manual/bulk-import` - Import CSV

---


## 🎯 Why This Project?

Traditional job application approaches have limitations:
- ❌ Manual job searching is time-consuming
- ❌ Hard to track which jobs were applied to
- ❌ Cover letter writing takes hours
- ❌ Difficult to assess job fit objectively

**AI Job Applier solves these problems:**
- ✅ Instant analysis of any job posting (3-5 seconds)
- ✅ AI-powered objective match scoring
- ✅ Auto-generated personalized cover letters
- ✅ Beautiful tracker UI with all applications
- ✅ Mobile access via Telegram bot
- ✅ No web scraping issues



## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🙏 Acknowledgments

- **Google Gemini AI** for powerful language model capabilities
- **Spring Boot** for excellent framework
- **Telegram** for bot API
- **Jsoup** for HTML parsing
- Open source community

---


## ⭐ Star This Repository

If you find this project helpful, please consider giving it a star! It helps others discover the project.

---

**Built with ❤️ using Spring Boot, Gemini AI, and lots of snacks **
