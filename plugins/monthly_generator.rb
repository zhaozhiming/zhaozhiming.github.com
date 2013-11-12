# Monthly Index Page Generator
#
# Monthly Archive on Octupress
# rcmdnk (https://github.com/rcmdnk/monthly-archive)

module Jekyll
  class MonthlyIndex < Page
    def initialize(site, base, period, posts)
      @site = site
      @base = base
      if site.config['permalink'].start_with?("/blog/:year/")
        archive_dir = "/blog"
      else
        archive_dir = ""
      end
      @dir  = "#{archive_dir}/#{period['year']}/#{period['month']}"
      @name = 'index.html'
      self.process(@name)
      self.read_yaml(File.join(base, '_layouts'), 'monthly_index.html')
      self.data['period'] = period
      self.data['period_posts'] = posts
      monthly_title_prefix = site.config['monthly_title_prefix'] || 'Archive: '

      month_format = '%b'
      upcase = true
      if site.config['monthly_format']
        if site.config['monthly_format'] == 'Jan'
          month_format = '%b'
          upcase = false
        elsif site.config['monthly_format'] == 'JAN'
          month_format = '%b'
          upcase = true
        elsif site.config['monthly_format'] == 'January'
          month_format = '%B'
          upcase = false
        elsif site.config['monthly_format'] == 'JANUARY'
          month_format = '%B'
          upcase = true
        elsif site.config['monthly_format'] == '01'
          month_format = '%m'
          upcase = false
        elsif site.config['monthly_format'] == '1'
          month_format = '%-m'
          upcase = false
        end
      end
      month_year = Time.new(period['year'],period['month']).strftime(month_format)\
              + " #{period['year']}"
      month_year = month_year.upcase if upcase
      self.data['title']       = "#{monthly_title_prefix}#{month_year}"
      self.data['description'] = "#{monthly_title_prefix}#{month_year}"
    end
  end

  class GenerateMontly < Generator
    safe true
    priority :low

    def generate(site)
      if site.layouts.key? 'monthly_index'
        site.posts.reverse.group_by{|c| {"month" => c.date.strftime('%m'),\
                                         "year" => c.date.strftime('%Y')}\
                                   }.each do |period, posts|
          write_archive_index(site, period, posts)
        end
      end
    end

    def write_archive_index(site, period, posts)
      index = MonthlyIndex.new(site, site.source, period, posts)
      index.render(site.layouts, site.site_payload)
      index.write(site.dest)
      site.pages << index
    end
  end
end
