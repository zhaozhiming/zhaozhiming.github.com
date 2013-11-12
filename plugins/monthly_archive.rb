# Monthly Archive on Octupress
# rcmdnk (https://github.com/rcmdnk/monthly-archive)


module Jekyll
  class MonthlyArchive < Liquid::Tag
    def initialize(tag_name, markup, tokens)
      @opts = {}
      @opts['counter'] = true
      @opts['first_open'] = false
      @opts['month_format'] = '%b'
      @opts['upcase'] = true

      if markup.strip =~ /\s*counter:(\w+)/i
        @opts['counter'] = ($1 == 'true')
        markup = markup.strip.sub(/counter:\w+/i,'')
      end
      if markup.strip =~ /\s*first_open:(\w+)/i
        @opts['first_open'] = ($1 == 'true')
        markup = markup.strip.sub(/first_open:\w+/i,'')
      end
      if markup.strip =~ /\s*month_format:(\w+)/i
        if $1 == 'Jan'
          @opts['month_format'] = '%b'
          @opts['upcase'] = false
        elsif $1 == 'JAN'
          @opts['month_format'] = '%b'
          @opts['upcase'] = true
        elsif $1 == 'January'
          @opts['month_format'] = '%B'
          @opts['upcase'] = false
        elsif $1 == 'JANUARY'
          @opts['month_format'] = '%B'
          @opts['upcase'] = true
        elsif $1 == '01'
          @opts['month_format'] = '%m'
          @opts['upcase'] = false
        elsif $1 == '1'
          @opts['month_format'] = '%-m'
          @opts['upcase'] = false
        end
        markup = markup.strip.sub(/month_format:\w+/i,'')
      end
      super
    end

    def render(context)
      config = context.registers[:site].config
      if config['permalink'].start_with?("/blog/:year/")
        archive_dir = "/blog"
      else
        archive_dir = ""
      end
      html = ""
      html << "<div class=\"monthly_archive\">"
      html << "<ul class=\"year_list\">"
      posts = context.registers[:site].posts.reverse
      posts_years = posts.group_by{|c| {"year" => c.date.year}}
      posts_years.each do |key_year, posts_year|
        html << "<li class=\"year"
        html << " first_open" if @opts['first_open']
        html << "\">#{key_year["year"]}"
        html << " (#{posts_year.count})" if @opts['counter']
        html << "</li>"
        html << "<ul class=\"month_list\">"
        posts_months = posts_year.group_by{|c| {\
          "month_format" => c.date.strftime(@opts['month_format']),\
          "month" => c.date.strftime('%m')}}

        posts_months.each do |key_month, posts_month|
          month = key_month["month_format"]
          month = month.upcase if @opts['upcase']
          html << "<li class=\"month\">"
          html << "<a href='#{archive_dir}/#{key_year["year"]}/#{key_month["month"]}/'>#{month}"
          html << " (#{posts_month.count})" if @opts['counter']
          html << "</a></li>"
        end
        html << "</ul>"
      end
      html << "</ul>"
      html << "</div>"
      html
    end
  end
end

Liquid::Template.register_tag('monthly_archive', Jekyll::MonthlyArchive)

