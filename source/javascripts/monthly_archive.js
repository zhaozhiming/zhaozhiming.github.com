jQuery(function($){
  $('.year').click(function(){
    $(this).next().slideToggle('fast', function(){
      if($(this).is(':hidden')){
        $(this).prev().removeClass('open');
      }else{
        $(this).prev().addClass('open');
      }
    });
  }).next().hide();
  $('.first_open:first').next().show(function(){
    $(this).prev().addClass('open');
  });
});
