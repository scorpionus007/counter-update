import { Controller, Get, Param, UseGuards } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { AdminApiKeyGuard } from './admin-api-key.guard';

@Controller('admin')
@UseGuards(AdminApiKeyGuard)
export class AdminController {
  constructor(private readonly prisma: PrismaService) {}

  @Get('users')
  async listUsers() {
    const users = await this.prisma.user.findMany({
      orderBy: { createdAt: 'desc' },
      include: {
        dailySummaries: {
          orderBy: { date: 'desc' },
          take: 7,
        },
        _count: { select: { unlockEvents: true } },
      },
    });

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return users.map((user) => {
      const todaySummary = user.dailySummaries.find(
        (s) => s.date.getTime() === today.getTime(),
      );
      const last7Total = user.dailySummaries.reduce((sum, s) => sum + s.unlockCount, 0);

      return {
        userId: user.id,
        name: user.name,
        phone: user.phone,
        kycStatus: user.kycStatus,
        createdAt: user.createdAt.toISOString(),
        todayCount: todaySummary?.unlockCount ?? 0,
        last7DaysTotal: last7Total,
        totalEvents: user._count.unlockEvents,
        recentDays: user.dailySummaries.map((s) => ({
          date: s.date.toISOString().slice(0, 10),
          unlockCount: s.unlockCount,
        })),
      };
    });
  }

  @Get('users/:userId/history')
  async userHistory(@Param('userId') userId: string) {
    const summaries = await this.prisma.dailyUnlockSummary.findMany({
      where: { userId },
      orderBy: { date: 'desc' },
      take: 30,
    });
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    return {
      user,
      history: summaries.map((s) => ({
        date: s.date.toISOString().slice(0, 10),
        unlockCount: s.unlockCount,
        payableAmountP: s.payableAmountP,
      })),
    };
  }
}
